package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.BedResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.CreateBedRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.CreateRoomRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.RoomResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.UpdateBedRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.UpdateRoomRequest;

import java.util.List;

public interface AdminRoomBedService {
    List<RoomResponse> getRoomList();
    RoomResponse createRoom(CreateRoomRequest request);
    RoomResponse updateRoomInfo(Long roomId, UpdateRoomRequest request);
    void deleteRoomById(Long roomId);
    
    List<BedResponse> getBedListByRoomId(Long roomId);
    BedResponse createBed(Long roomId, CreateBedRequest request);
    BedResponse updateBedStatus(Long bedId, UpdateBedRequest request);
}
