package com.database.societyproject.security.config;

import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.security.entity.UserSocietyRole;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * One-way compatibility migration for data created before the simplified role
 * model. It preserves people who already had the two equivalent roles and
 * disables every retired role so old JWTs and old screens cannot retain access.
 */
@Configuration
public class RoleSimplificationInitializer {
    @Bean
    CommandLineRunner simplifyLegacyRoles(UserSocietyRoleRepository roles, FlatOccupancyRepository occupancies,
                                          DataSource dataSource) {
        return args -> {
            ensurePostgresRoleConstraint(dataSource);
            migrate(roles, occupancies);
        };
    }

    @Transactional
    void migrate(UserSocietyRoleRepository roles, FlatOccupancyRepository occupancies) {
        for (UserSocietyRole assignment : roles.findAll()) {
            UserRole replacement = switch (assignment.getRole()) {
                case SOCIETY_MANAGER -> UserRole.SOCIETY_ADMIN;
                case RESIDENT -> hasPrimaryOwnerHome(assignment, occupancies) ? UserRole.FLAT_OWNER : null;
                default -> null;
            };
            if (replacement != null && Boolean.TRUE.equals(assignment.getActive())
                    && roles.findByUserIdAndSocietyIdAndRole(assignment.getUser().getId(),
                    assignment.getSociety().getId(), replacement).isEmpty()) {
                UserSocietyRole migrated = new UserSocietyRole();
                migrated.setUser(assignment.getUser());
                migrated.setSociety(assignment.getSociety());
                migrated.setRole(replacement);
                migrated.setActive(true);
                roles.save(migrated);
            }
            if (!assignment.getRole().isCurrentProductRole() && Boolean.TRUE.equals(assignment.getActive())) {
                assignment.setActive(false);
                roles.save(assignment);
            }
        }
    }

    private boolean hasPrimaryOwnerHome(UserSocietyRole assignment, FlatOccupancyRepository occupancies) {
        return occupancies.existsByResidentUserIdAndFlatBlockSocietyIdAndOccupancyTypeAndResidentRoleAndActiveTrue(
                assignment.getUser().getId(), assignment.getSociety().getId(),
                com.database.societyproject.occupancy.entity.OccupancyType.OWNER,
                com.database.societyproject.occupancy.entity.ResidentRole.PRIMARY);
    }

    private void ensurePostgresRoleConstraint(DataSource dataSource) {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            if (!connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres")) return;
            // Retired values remain legal only to preserve historical rows. They
            // cannot be granted or authorized anywhere in the application.
            String allowedRoles = "'ADMIN','SOCIETY_ADMIN','FLAT_OWNER','LOGBOOK_OPERATOR',"
                    + "'SOCIETY_MANAGER','FINANCE_MANAGER','SECURITY_MANAGER','BLOCK_MANAGER','RESIDENT','STAFF','SECURITY'";
            statement.execute("ALTER TABLE user_society_roles DROP CONSTRAINT IF EXISTS user_society_roles_role_check");
            statement.execute("ALTER TABLE user_society_roles ADD CONSTRAINT user_society_roles_role_check "
                    + "CHECK (role IN (" + allowedRoles + "))");

            // Hibernate creates enum checks for new tables, but it does not widen
            // an existing check when enum values are added. Invitations pre-date
            // FLAT_OWNER and LOGBOOK_OPERATOR, so without this migration PostgreSQL
            // rejects every new simplified-role invitation as a generic 409.
            statement.execute("ALTER TABLE society_invitations DROP CONSTRAINT IF EXISTS society_invitations_invited_role_check");
            statement.execute("ALTER TABLE society_invitations ADD CONSTRAINT society_invitations_invited_role_check "
                    + "CHECK (invited_role IN (" + allowedRoles + "))");

            // The notification enum also grew during simplification. Widen the
            // pre-existing PostgreSQL check so approved policies can notify owners.
            statement.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check");
            statement.execute("ALTER TABLE notifications ADD CONSTRAINT notifications_type_check "
                    + "CHECK (type IN ('MAINTENANCE_DUE','MAINTENANCE_POLICY_CHANGED','PAYMENT_CONFIRMED',"
                    + "'COMPLAINT_UPDATE','VISITOR_ALERT','ANNOUNCEMENT'))");
        } catch (Exception exception) {
            throw new IllegalStateException("Could not prepare the simplified role migration", exception);
        }
    }
}
