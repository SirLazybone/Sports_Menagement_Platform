package com.course_work.Sports_Menagement_Platform.mapper;

import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.OrgComDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserOrgComDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrgComMapper {
    public OrgCom DTOToEntity(OrgComDTO orgComDTO) {
        return OrgCom.builder().name(orgComDTO.getName()).build();
    }
}
