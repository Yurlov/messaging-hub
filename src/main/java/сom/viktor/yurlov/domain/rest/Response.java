package сom.viktor.yurlov.domain.rest;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response extends ResponseEntity {
    Integer code;
    String message;
    Object body;
    @JsonRawValue
    String extra;

    public Response(HttpStatus status) {
        super(status);
    }

    public Response(Object body, HttpStatus status) {
        super(body, status);
    }

    public Response(MultiValueMap headers, HttpStatus status) {
        super(headers, status);
    }

    public Response(Object body, MultiValueMap headers, HttpStatus status) {
        super(body, headers, status);
    }
}
