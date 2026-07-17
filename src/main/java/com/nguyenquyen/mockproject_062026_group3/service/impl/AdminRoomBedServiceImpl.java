package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.BedResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.CreateBedRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.CreateRoomRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.RoomResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.UpdateBedRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.room.UpdateRoomRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.Bed;
import com.nguyenquyen.mockproject_062026_group3.entity.Facility;
import com.nguyenquyen.mockproject_062026_group3.entity.Room;
import com.nguyenquyen.mockproject_062026_group3.repository.BedRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.FacilityRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.RoomRepository;
import com.nguyenquyen.mockproject_062026_group3.service.AdminRoomBedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRoomBedServiceImpl implements AdminRoomBedService {

    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final FacilityRepository facilityRepository;

    private static final Long FACILITY_ID = 1L; // Assuming single facility context for admin ops

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomList() {
        List<Room> rooms = roomRepository.findByFacilityIdAndIsDeletedFalse(FACILITY_ID);
        return rooms.stream().map(this::mapToRoomResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        if (roomRepository.existsByFacilityIdAndRoomNumberAndIsDeletedFalse(FACILITY_ID, request.getRoomNumber())) {
            throw new RuntimeException("Room number already exists in this facility");
        }

        Facility facility = facilityRepository.findById(FACILITY_ID)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .roomType(request.getRoomType())
                .facility(facility)
                .isDeleted(false)
                .build();

        room = roomRepository.save(room);
        return mapToRoomResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse updateRoomInfo(Long roomId, UpdateRoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (request.getRoomNumber() != null && !request.getRoomNumber().equals(room.getRoomNumber())) {
            if (roomRepository.existsByFacilityIdAndRoomNumberAndIsDeletedFalse(FACILITY_ID, request.getRoomNumber())) {
                throw new RuntimeException("Room number already exists in this facility");
            }
            room.setRoomNumber(request.getRoomNumber());
        }

        if (request.getRoomType() != null) {
            room.setRoomType(request.getRoomType());
        }

        room = roomRepository.save(room);
        return mapToRoomResponse(room);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (bedRepository.existsByRoomIdAndStatus(roomId, "OCCUPIED")) {
            throw new RuntimeException("Cannot delete room with occupied beds");
        }
        
        room.setIsDeleted(true);
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedResponse> getBedListByRoomId(Long roomId) {
        List<Bed> beds = bedRepository.findByRoomId(roomId);
        return beds.stream().map(this::mapToBedResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BedResponse createBed(Long roomId, CreateBedRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (bedRepository.existsByRoomIdAndBedNumber(roomId, request.getBedNumber())) {
            throw new RuntimeException("Bed number already exists in this room");
        }

        Bed bed = Bed.builder()
                .bedNumber(request.getBedNumber())
                .status(request.getStatus() != null ? request.getStatus() : "AVAILABLE")
                .room(room)
                .build();

        bed = bedRepository.save(bed);
        return mapToBedResponse(bed);
    }

    @Override
    @Transactional
    public BedResponse updateBedStatus(Long bedId, UpdateBedRequest request) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new RuntimeException("Bed not found"));

        if (request.getBedNumber() != null && !request.getBedNumber().equals(bed.getBedNumber())) {
             if (bedRepository.existsByRoomIdAndBedNumber(bed.getRoom().getId(), request.getBedNumber())) {
                throw new RuntimeException("Bed number already exists in this room");
            }
            bed.setBedNumber(request.getBedNumber());
        }

        if (request.getStatus() != null) {
            bed.setStatus(request.getStatus());
        }

        bed = bedRepository.save(bed);
        return mapToBedResponse(bed);
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .facilityId(room.getFacility() != null ? room.getFacility().getId() : null)
                .isDeleted(room.getIsDeleted())
                .build();
    }

    private BedResponse mapToBedResponse(Bed bed) {
        return BedResponse.builder()
                .id(bed.getId())
                .bedNumber(bed.getBedNumber())
                .status(bed.getStatus())
                .roomId(bed.getRoom() != null ? bed.getRoom().getId() : null)
                .build();
    }
}
