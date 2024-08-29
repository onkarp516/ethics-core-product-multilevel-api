package in.truethics.ethics.ethicsapiv10.model.dispatch_management;


import antlr.NameSpace;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transport_agency_tbl")
public class TransportAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String transportAgencyName;
    private String address;
    private Long contactNo;
    private String contactPerson;
//    private String city;
//    private String state;
    private String pincode;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JsonIgnoreProperties(value = {"transportAgency", "hibernateLazyInitializer"})
//    @JoinColumn(name = "country_id", nullable = false)
//    private Country country;
    private Long countryId;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JsonIgnoreProperties(value = {"transportAgency", "hibernateLazyInitializer"})
//    @JoinColumn(name = "state_id", nullable = false)
//    private State state;
    private Long stateId;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JsonIgnoreProperties(value = {"transportAgency", "hibernateLazyInitializer"})
//    @JoinColumn(name = "city_id", nullable = false)
//    private City city;
    private Long cityId;

    @CreationTimestamp
    private LocalDateTime createdAt;
    private Long createdBy;
    private Boolean status;

//    @ManyToOne
//    @JoinColumn(name = "branch_id")
//    @JsonManagedReference
//    private Branch branch;
    private Long branchId;

//    @ManyToOne
//    @JoinColumn(name = "outlet_id")
//    @JsonManagedReference
//    private Outlet outlet;
    private Long outletId;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private Long updatedBy;


//    public void setCountry(Country country) {
//    }
//
//    public void setCity(String valueOf) {
//    }
//
//    public void setState(String valueOf) {
//    }
}
