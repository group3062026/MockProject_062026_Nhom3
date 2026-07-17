package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.BedResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.CreateBedRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.CreateRoomRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.RoomResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.UpdateBedRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.UpdateRoomRequest;
import com.nguyenquyen.mockproject_062026_group3.service.AdminRoomBedService;
import com.nguyenquyen.mockproject_062026_group3.common.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/admin/facility-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NHA_Admin')")
public class AdminRoomBedController {

    private final AdminRoomBedService adminRoomBedService;

    @GetMapping("/rooms")
    public ApiResponse<List<RoomResponse>> getRoomList() {
        return ApiResponse.success(adminRoomBedService.getRoomList());
    }

    @PostMapping("/rooms")
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ApiResponse.success(adminRoomBedService.createRoom(request));
    }

    @PutMapping("/rooms/{roomId}")
    public ApiResponse<RoomResponse> updateRoomInfo(@PathVariable Long roomId, @Valid @RequestBody UpdateRoomRequest request) {
        return ApiResponse.success(adminRoomBedService.updateRoomInfo(roomId, request));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ApiResponse<Void> deleteRoomById(@PathVariable Long roomId) {
        adminRoomBedService.deleteRoomById(roomId);
        return ApiResponse.success(null);
    }

    @GetMapping("/rooms/{roomId}/beds")
    public ApiResponse<List<BedResponse>> getBedListByRoomId(@PathVariable Long roomId) {
        return ApiResponse.success(adminRoomBedService.getBedListByRoomId(roomId));
    }

    @PostMapping("/rooms/{roomId}/beds")
    public ApiResponse<BedResponse> createBed(@PathVariable Long roomId, @Valid @RequestBody CreateBedRequest request) {
        return ApiResponse.success(adminRoomBedService.createBed(roomId, request));
    }

    @PutMapping("/beds/{bedId}")
    public ApiResponse<BedResponse> updateBedStatus(@PathVariable Long bedId, @Valid @RequestBody UpdateBedRequest request) {
        return ApiResponse.success(adminRoomBedService.updateBedStatus(bedId, request));
    }
}
