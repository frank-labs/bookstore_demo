package ca.uwo.bookstore.controller.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserBookPair {
    @NotBlank
    public int userid;

    @NotBlank
    public int bookid;
}
