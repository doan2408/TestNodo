package com.example.coursemanagement.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentRequest {
    @NotBlank(message = "{student.name.required}")
    @Size(min = 2, max = 255, message = "{student.name.size}")
    private String name;

    private String gender;

    @NotBlank(message = "{student.email.required}")
    @Email(message = "{student.email.invalid}")
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "{student.phone.invalid}")
    private String phone;

    private List<MultipartFile> avatar;

    private List<Long> deleteAvatarIds;
}
