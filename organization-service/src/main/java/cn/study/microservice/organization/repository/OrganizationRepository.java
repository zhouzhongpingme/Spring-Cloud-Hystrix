package cn.study.microservice.organization.repository;

import cn.study.microservice.organization.domain.Organization;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends CrudRepository<Organization,String> {
    public Organization findById(String organizationId);
}
