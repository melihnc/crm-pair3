package com.tcellpair3.customerservice.service.concretes;

import com.tcellpair3.customerservice.clients.AddressClient;
import com.tcellpair3.customerservice.core.dtos.requests.customer.CreateCustomerRequest;
import com.tcellpair3.customerservice.core.dtos.requests.customer.UpdateCustomerRequest;
import com.tcellpair3.customerservice.core.dtos.responses.address.CustomerWithAddressesResponse;
import com.tcellpair3.customerservice.core.dtos.responses.address.GetAllAddressResponse;
import com.tcellpair3.customerservice.core.dtos.responses.customer.*;
import com.tcellpair3.customerservice.core.exception.type.BusinessException;
import com.tcellpair3.customerservice.core.exception.type.IllegalArgumentException;
import com.tcellpair3.customerservice.core.mappers.AddressMapper;
import com.tcellpair3.customerservice.core.mappers.CustomerMapper;
import com.tcellpair3.customerservice.core.mernis.IRKKPSPublicSoap;
import com.tcellpair3.customerservice.core.service.Abstract.ContactMediumValidationService;
import com.tcellpair3.customerservice.core.service.Concrete.CustomerValidationServiceImpl;
import com.tcellpair3.customerservice.entities.Address;
import com.tcellpair3.customerservice.entities.Customer;
import com.tcellpair3.customerservice.entities.CustomerInvoice;
import com.tcellpair3.customerservice.repositories.CustomerInvoiceRepository;
import com.tcellpair3.customerservice.repositories.CustomerRepository;
import com.tcellpair3.customerservice.service.abstracts.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerValidationServiceImpl customerValidationService;
    private final ContactMediumValidationService contactMediumValidationService;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final AddressClient addressClient;
    @Override
    public CreateCustomerResponse createCustomer(CreateCustomerRequest request) throws Exception {

        IRKKPSPublicSoap client = new IRKKPSPublicSoap();

        boolean isRealPerson = client.TCKimlikNoDogrula(
                Long.valueOf(request.getNationalId()),
                request.getFirstName(),
                request.getLastName(),
                Integer.valueOf(request.getBirthdate().getYear())
        );

        if(isRealPerson){
            boolean hasNationalId =customerRepository.existsByNationalId(request.getNationalId());
            if(hasNationalId)
            {
                throw new BusinessException("A customer is already exist with this Nationality ID");
            }
            customerValidationService.validateBirthdate(request.getBirthdate());

            Customer customer= CustomerMapper.INSTANCE.createCustomerMapper(request);
            Customer saveCustomer = customerRepository.save(customer);

            return new CreateCustomerResponse(
                    saveCustomer.getId(),
                    saveCustomer.getAccountNumber(),
                    saveCustomer.getFirstName(),
                    saveCustomer.getLastName(),
                    saveCustomer.getMiddleName(),
                    saveCustomer.getNationalId(),
                    saveCustomer.getMotherName(),
                    saveCustomer.getFatherName(),
                    saveCustomer.getBirthdate(),
                    saveCustomer.getGender()
            );
        }

        else {
            throw new IllegalArgumentException("Kullanıcı bulunamadı");
        }

        /*boolean hasNationalId =customerRepository.existsByNationalId(request.getNationalId());
        if(hasNationalId)
        {
            throw new BusinessException("A customer is already exist with this Nationality ID");
        }
        customerValidationService.validateBirthdate(request.getBirthdate());
        customerValidationService.isValidTC(request.getNationalId());
        // Birthday check
        Customer customer= CustomerMapper.INSTANCE.createCustomerMapper(request);
        Customer saveCustomer = customerRepository.save(customer);

         */



    }

    @Override
    public UpdateCustomerResponse updateCustomer(int id, UpdateCustomerRequest request) throws Exception {
        IRKKPSPublicSoap client = new IRKKPSPublicSoap();

        boolean isRealPerson = client.TCKimlikNoDogrula(
                Long.valueOf(request.getNationalId()),
                request.getFirstName(),
                request.getLastName(),
                Integer.valueOf(request.getBirthdate().getYear())
        );

        if(isRealPerson){
            boolean hasNationalId =customerRepository.existsByNationalId(request.getNationalId());
            if(hasNationalId)
            {
                throw new BusinessException("A customer is already exist with this Nationality ID");
            }
        Optional<Customer> customerOptional = customerRepository.findById(id);
        Customer existingCustomer = customerOptional.get();

        List<CustomerInvoice> invoices = customerInvoiceRepository.findByCustomerId(customerOptional.get().getId());
        for (CustomerInvoice invoice : invoices) {
            invoice.setAccountName(request.getAccountNumber());
            customerInvoiceRepository.save(invoice);
        }
        customerValidationService.validateBirthdate(request.getBirthdate());
        customerValidationService.isValidTC(request.getNationalId());
        Customer customer = CustomerMapper.INSTANCE.updateCustomerMapper(request,existingCustomer);
        Customer saveCustomer=customerRepository.save(customer);

        return new UpdateCustomerResponse(
                saveCustomer.getId(),
                saveCustomer.getAccountNumber(),
                saveCustomer.getFirstName(),
                saveCustomer.getLastName(),
                saveCustomer.getMiddleName(),
                saveCustomer.getNationalId(),
                saveCustomer.getMotherName(),
                saveCustomer.getFatherName(),
                saveCustomer.getBirthdate(),
                saveCustomer.getGender()

                );

    }

    else{
        throw new IllegalArgumentException("Kullanıcı bulunamadı");
        }

    }

    @Override
    public void deleteCustomer(int id) {

        customerRepository.deleteById(id);

    }

    @Override
    public List<GetAllCustomersResponse> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return CustomerMapper.INSTANCE.customersToListCustomerResponses(customers);
    }

    @Override
    public Optional<GetByIdCustomerResponse> getByCustomerId(int id) {
        Optional<Customer> customerOptional= customerRepository.findById(id);
        return customerOptional.map(CustomerMapper.INSTANCE::getByIdCustomerMapper);
    }

    @Override
    public Page<SearchResultsResponse> getCustomersByFirstName(String firstName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = customerRepository.findByFirstNameStartingWithIgnoreCase(firstName, pageable);
        return customerPage.map(CustomerMapper.INSTANCE::searchResultResponse);
    }
    @Override
    public List<SearchResultsResponse> findByFirstName(String firstName) {
        List<Customer> customers = customerRepository.findByFirstName(firstName);
        if(customers.isEmpty())
        {
            throw new BusinessException("No customer found! Would you like to create the customer?");
        }
        return customers.stream()
                .map(CustomerMapper.INSTANCE::searchResultResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultsResponse> findByLastName(String lastName) {
        List<Customer> customers = customerRepository.findByLastName(lastName);
        if(customers.isEmpty())
        {
            throw new BusinessException("No customer found! Would you like to create the customer?");
        }
        return customers.stream()
                .map(CustomerMapper.INSTANCE::searchResultResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultsResponse> findByAccountNumber(Integer accountNumber) {
        List<Customer> customers = customerRepository.findByAccountNumber(accountNumber);
        if(customers.isEmpty())
        {
            throw new BusinessException("No customer found! Would you like to create the customer?");
        }
        return customers.stream()
                .map(CustomerMapper.INSTANCE::searchResultResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultsResponse> findByNationalId(String nationalId) {
        List<Customer> customers = customerRepository.findByNationalId(nationalId);
        if(customers.isEmpty())
        {
            throw new BusinessException("No customer found! Would you like to create the customer?");
        }
        return customers.stream()
                .map(CustomerMapper.INSTANCE::searchResultResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultsResponse> findByContactMedium_MobilePhone(String mobilePhone) {
        contactMediumValidationService.validatePhoneNumber(mobilePhone);
        List<Customer> customers = customerRepository.findByContactMedium_MobilePhone(mobilePhone);
        if(customers.isEmpty())
        {
            throw new BusinessException("No customer found! Would you like to create the customer?");
        }
        return customers.stream()
                .map(CustomerMapper.INSTANCE::searchResultResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetAllAddressResponse> findAddressesByCustomerId(Integer customerId) {
        List<Address> addresses = customerRepository.findAddressesByCustomerId(customerId);
        return AddressMapper.INSTANCE.AddressToListAddressResponses(addresses);
    }

    @Override
    public CustomerWithAddressesResponse getCustomerWithAddresses(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<com.tcellpair3.addressservice.entities.Address> addresses = addressClient.getAddressesByCustomerId(customerId);

        return new CustomerWithAddressesResponse(customer, addresses);
    }

    @Override
    public boolean existsById(Integer customerId) {
        return customerRepository.existsById(customerId);
    }


}
