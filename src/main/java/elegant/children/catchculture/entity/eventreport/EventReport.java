package elegant.children.catchculture.entity.eventreport;


import elegant.children.catchculture.entity.culturalevent.CulturalEventDetail;
import elegant.children.catchculture.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_report_id")
    private int id;



    @Embedded
    private CulturalEventDetail culturalEventDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


}
