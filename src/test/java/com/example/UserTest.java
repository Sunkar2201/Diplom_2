package com.example;
import com.example.models.responses.ErrorResponseModel;
import com.example.models.responses.user.UserGetCreateResponseModel;
import com.example.models.responses.user.UserPatchResponseModel;
import com.example.models.responses.user.UserResponseModel;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {
    public UserApi userApi = new UserApi();

    public static final String EMAIL = "kussayinovsunkar@yandex.ru";
    public static final String PASSWORD = "123";
    public static final String USERNAME = "sunkar2201";

    @Test
    public void testCreateUser() {
        Response response = userApi.createUser(
                EMAIL,
                PASSWORD,
                USERNAME
        );
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testCreateDuplicateUser() {
        Response responseFirstUser = userApi.createUser(
                EMAIL,
                PASSWORD,
                USERNAME
        );
        assertEquals(200, responseFirstUser.statusCode());

        Response responseSecondUser = userApi.createUser(EMAIL, PASSWORD, USERNAME);
        assertEquals(403, responseSecondUser.statusCode());
    }

    @Test
    public void testLogin() {
        Response responseCreateUser = userApi.createUser(
                EMAIL,
                PASSWORD,
                USERNAME
        );
        assertEquals(200, responseCreateUser.statusCode());

        Response responseGetUser = userApi.getUser(EMAIL, PASSWORD);
        assertEquals(200, responseGetUser.statusCode());
    }

    @Test
    public void testLoginWithIncorrectData() {
        Response responseCreateUser = userApi.createUser(
                EMAIL,
                PASSWORD,
                USERNAME
        );
        assertEquals(200, responseCreateUser.statusCode());

        Response responseGetUser = userApi.getUser("gdfdfd@qwe.kz", "1");
        assertEquals(401, responseGetUser.statusCode());

        ErrorResponseModel error = responseGetUser.then().extract().body().as(ErrorResponseModel.class);
        assertFalse(error.isSuccess());
        assertEquals(error.getMessage(), "email or password are incorrect");
    }

    @ParameterizedTest
    @CsvSource({
            ",123,sunkar2201",  // Пропущен email
            "kussayinovsunkar@yandex.ru,,sunkar2201",  // Пропущен пароль
            "kussayinovsunkar@yandex.ru,123,"  // Пропущено имя
    })
    public void testInvalidUserCreation(String email, String password, String name) {
        Response response = userApi.createUser(email, password, name);
        assertEquals(403, response.statusCode());
    }

    @Test
    public void testChangeUserData() {
        // Создание пользователя.
        Response responseCreateUser = userApi.createUser(
                EMAIL,
                PASSWORD,
                USERNAME
        );
        assertEquals(200, responseCreateUser.statusCode());
        String token = responseCreateUser.then().extract().body().as(UserGetCreateResponseModel.class).getAccessToken();


        // Изменение пользователя.
        Response responsePatchUser = userApi.patchUser(
                EMAIL + "t",
                PASSWORD + "4",
                USERNAME + "b",
                token
        );
        assertEquals(200, responsePatchUser.statusCode());

        UserPatchResponseModel actualResponse = responsePatchUser.then().extract().body().
                as(UserPatchResponseModel.class);

        UserPatchResponseModel expectedResponse = new UserPatchResponseModel(
                true,
                new UserResponseModel(EMAIL + "t", USERNAME + "b")
        );

        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);

        // Повторное изменения, чтобы удалить.
        Response responseSecondPatchUser = userApi.patchUser(
                EMAIL,
                PASSWORD,
                USERNAME,
                token
        );
        assertEquals(200, responseSecondPatchUser.statusCode());
    }

    @Test
    public void testUnauthChangeUserData() {
        // Создание пользователя.
        Response responseCreateUser = userApi.createUser(
                EMAIL,
                PASSWORD,
                USERNAME
        );
        assertEquals(200, responseCreateUser.statusCode());
        String token = responseCreateUser.then().extract().body().as(UserGetCreateResponseModel.class).getAccessToken();


        // Изменение пользователя без авторизации.
        Response response = userApi.patchUser(
                EMAIL + "t",
                PASSWORD + "4",
                USERNAME + "b",
                "token"
        );
        assertEquals(401, response.statusCode());
        ErrorResponseModel error = response.then().extract().as(ErrorResponseModel.class);
        assertFalse(error.isSuccess());
        assertEquals(error.getMessage(), "You should be authorised");
    }

    @AfterEach
    @Step("tearDown")
    public void tearDown() {
        UserGetCreateResponseModel userGetResponse = userApi.getUser(
                EMAIL,
                PASSWORD
        ).as(UserGetCreateResponseModel.class);
        if (userGetResponse.isSuccess()) {
            Response response = userApi.deleteUser(userGetResponse.getAccessToken());
            assertEquals(202, response.statusCode());
        }
    }
}