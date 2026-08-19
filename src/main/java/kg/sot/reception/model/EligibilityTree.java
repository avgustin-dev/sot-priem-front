package kg.sot.reception.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kg.sot.reception.dto.EligibilityNode;
import kg.sot.reception.util.converter.EligibilityNodeListConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eligibility_tree")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityTree {

    public static final int SINGLETON_ID = 1;

    @Id
    private Integer id;

    @Convert(converter = EligibilityNodeListConverter.class)
    @Column(columnDefinition = "text", nullable = false)
    @Builder.Default
    private List<EligibilityNode> nodes = new ArrayList<>();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
