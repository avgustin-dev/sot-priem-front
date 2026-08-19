package kg.sot.reception.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kg.sot.reception.util.converter.StringObjectMapConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "site_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteContent {

    public static final int SINGLETON_ID = 1;

    @Id
    private Integer id;

    @Convert(converter = StringObjectMapConverter.class)
    @Column(columnDefinition = "text", nullable = false)
    @Builder.Default
    private Map<String, Object> content = new LinkedHashMap<>();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
