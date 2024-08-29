package in.truethics.ethics.ethicsapiv10.model.dispatch_management;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
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
@Table(name = "delivery_boy_tbl")
public class DeliveryBoy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String firstName;
    private String lastName;
    private Long mobileNo;
    private String address;
    private String identityDocument;
//    private String imagePath;


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



}
