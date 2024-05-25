package com.tcellpair3.customerservice.service.abstracts;

import com.tcellpair3.customerservice.core.dtos.requests.customer.CreateCustomerRequest;
import com.tcellpair3.customerservice.core.dtos.requests.customer.UpdateCustomerRequest;
import com.tcellpair3.customerservice.core.dtos.responses.address.CustomerWithAddressesResponse;
import com.tcellpair3.customerservice.core.dtos.responses.address.GetAllAddressResponse;
import com.tcellpair3.customerservice.core.dtos.responses.customer.*;
import com.tcellpair3.customerservice.entities.Address;
import com.tcellpair3.customerservice.entities.Customer;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    CreateCustomerResponse createCustomer(CreateCustomerRequest request) throws Exception;
    UpdateCustomerResponse updateCustomer(int id, UpdateCustomerRequest request) throws Exception;
    void deleteCustomer(int id);
    List<GetAllCustomersResponse> getAllCustomers();
    Optional<GetByIdCustomerResponse> getByCustomerId(int id);
    public Page<SearchResultsResponse> getCustomersByFirstName(String firstName, int page, int size);
    List<SearchResultsResponse> findByFirstName(String firstName);
    List<SearchResultsResponse> findByLastName(String lastName);
    List<SearchResultsResponse> findByAccountNumber(Integer accountNumber);
    List<SearchResultsResponse> findByNationalId(String nationalId);
    List<SearchResultsResponse> findByContactMedium_MobilePhone(String mobilePhone);
    List<GetAllAddressResponse> findAddressesByCustomerId(Integer customerId);


    CustomerWithAddressesResponse getCustomerWithAddresses(Integer customerId);
    boolean existsById(Integer customerId);
}
