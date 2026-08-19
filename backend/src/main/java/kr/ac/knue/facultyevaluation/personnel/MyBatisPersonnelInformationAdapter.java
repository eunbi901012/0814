package kr.ac.knue.facultyevaluation.personnel;

import java.util.List;
import kr.ac.knue.facultyevaluation.personnel.mapper.PersonnelInformationMapper;
import org.springframework.stereotype.Component;

@Component
public class MyBatisPersonnelInformationAdapter implements PersonnelInformationPort {

    private final PersonnelInformationMapper personnelInformationMapper;

    public MyBatisPersonnelInformationAdapter(PersonnelInformationMapper personnelInformationMapper) {
        this.personnelInformationMapper = personnelInformationMapper;
    }

    @Override
    public List<PersonnelSnapshot> search(PersonnelSearchCriteria criteria) {
        return personnelInformationMapper.searchReadonly(criteria);
    }
}
