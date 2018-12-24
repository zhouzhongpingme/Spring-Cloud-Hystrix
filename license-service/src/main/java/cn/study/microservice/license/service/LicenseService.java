package cn.study.microservice.license.service;

import cn.study.microservice.license.client.OrganizationDiscoveryClient;
import cn.study.microservice.license.client.OrganizationFeignClient;
import cn.study.microservice.license.client.OrganizationRestTemplateClient;
import cn.study.microservice.license.config.ServiceConfig;
import cn.study.microservice.license.domain.License;
import cn.study.microservice.license.domain.Organization;
import cn.study.microservice.license.repository.LicenseRepository;
import cn.study.microservice.license.utils.UserContextHolder;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class LicenseService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

    @Autowired
    private ServiceConfig serviceConfig;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private OrganizationDiscoveryClient organizationDiscoveryClient;

    @Autowired
    private OrganizationRestTemplateClient organizationRestClient;

    @Autowired
    OrganizationFeignClient organizationFeignClient;

    private Organization retrieveOrgInfo(String organizationId, String clientType){
        Organization organization = null;

        switch (clientType) {
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = organizationRestClient.getOrganization(organizationId);
                break;
            case "feign":
                System.out.println("I am using the feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationDiscoveryClient.getOrganization(organizationId);
        }

        return organization;
    }

    public License getLicense(String organizationId, String licenseId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);

        Organization org = retrieveOrgInfo(organizationId, clientType);

        return license
                .withOrganizationName( org.getName())
                .withContactName( org.getContactName())
                .withContactEmail( org.getContactEmail() )
                .withContactPhone( org.getContactPhone() )
                .withComment(serviceConfig.getExampleProperty());
    }

    public License getLicense(String organizationId,String licenseId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);

        Organization org = getOrganization(organizationId);

        return license
                .withOrganizationName( org.getName())
                .withContactName( org.getContactName())
                .withContactEmail( org.getContactEmail() )
                .withContactPhone( org.getContactPhone() )
                .withComment(serviceConfig.getExampleProperty());
    }

    public Organization getOrganization(String organizationId) {
        randomlyRunLong();
        return organizationRestClient.getOrganization(organizationId);
    }

    /*@HystrixCommand(
        commandProperties= {
            @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="12000")
        }
    )*/
    @HystrixCommand(
        fallbackMethod = "buildFallbackLicenseList",
        threadPoolKey = "licenseByOrgThreadPool",
        threadPoolProperties = {
            @HystrixProperty(name = "coreSize",value="30"),
            @HystrixProperty(name="maxQueueSize", value="10")
        },
        commandProperties = {
            @HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="75"),
            @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="7000"),
            @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds",value="15000"),
            @HystrixProperty(name="metrics.rollingStats.numBuckets",value="5")
        }
    )
    public List<License> getLicensesByOrg(String organizationId){
        logger.info("LicenseService.getLicensesByOrg  Correlation id: {}", UserContextHolder.getContext().getCorrelationId());
        randomlyRunLong();
        return licenseRepository.findByOrganizationId(organizationId);
    }

    private List<License> buildFallbackLicenseList(String organizationId){
        List<License> fallbackList = new ArrayList<>();
        License license = new License()
                .withId("0000000-00-00000")
                .withOrganizationId( organizationId )
                .withProductName("Sorry no licensing information currently available");
        fallbackList.add(license);
        return fallbackList;
    }

    private void randomlyRunLong(){
        Random rand = new Random();
        int randomNum = rand.nextInt((3 - 1) + 1) + 1;
        if (randomNum==3) sleep();
    }

    private void sleep(){
        try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
