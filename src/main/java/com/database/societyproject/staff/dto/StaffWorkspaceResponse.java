package com.database.societyproject.staff.dto;

import com.database.societyproject.complaint.dto.ComplaintResponse;
import java.util.List;

public record StaffWorkspaceResponse(List<StaffAssignmentResponse> assignments, List<ComplaintResponse> complaints) { }
