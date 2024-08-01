package expresstalk.dev.backend.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "private_messages")
@NoArgsConstructor
@Data
public class PrivateMessage extends Message {
    @ManyToOne
    @JoinColumn(name = "senderId", referencedColumnName = "id")
    private PrivateChatAccount sender;

    @ManyToOne
    @JoinColumn(name = "receiverId", referencedColumnName = "id")
    private PrivateChatAccount receiver;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "privateChatId", referencedColumnName = "id")
    @JsonIgnore
    private PrivateChat privateChat;

    public PrivateMessage(PrivateChatAccount sender, PrivateChatAccount receiver, PrivateChat privateChat, String content, Date createdAt) {
        super(content, createdAt);
        this.sender = sender;
        this.receiver = receiver;
        this.privateChat = privateChat;
    }
}
