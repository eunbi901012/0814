package kr.ac.knue.facultyevaluation.personnel;

import java.util.List;

public interface PersonnelInformationPort {

    List<PersonnelSnapshot> search(PersonnelSearchCriteria criteria);
}
