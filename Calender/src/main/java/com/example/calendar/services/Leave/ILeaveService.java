package com.example.calendar.services.Leave;


import com.example.calendar.entities.Leave.Leave;

import java.util.List;


public interface ILeaveService {
    int[] calculDaysLeft(String instructorId);

    public Leave addLeave(Leave leave , String instructorId);

    // update by instructor
    Leave updateLeave(Leave leave, String leaveId, String instructorId);
    public void deleteLeave(String leaveId,String instructorId);
    public List<Leave> allLeave();
    public List<Leave> leaveBystatus(String status);
    public Leave acceptLeave(String leaveId);
    public Leave refuseLeave(String leaveId);
    public int countPendingLeaves();
    public int NbrPeopLeaveToday();
    public List<Leave> getLeaveByInstructorId(String instructorId);
    public String getInstructorDetailsForLeave(String leaveId);






    }
