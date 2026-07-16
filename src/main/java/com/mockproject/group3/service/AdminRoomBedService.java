package com.mockproject.group3.service;

import com.mockproject.group3.dto.admin.room.BedResponse;
import com.mockproject.group3.dto.admin.room.CreateBedRequest;
import com.mockproject.group3.dto.admin.room.CreateRoomRequest;
import com.mockproject.group3.dto.admin.room.RoomResponse;
import com.mockproject.group3.dto.admin.room.UpdateBedRequest;
import com.mockproject.group3.dto.admin.room.UpdateRoomRequest;

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
