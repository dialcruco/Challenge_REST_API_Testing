package ChallengeAPI;

import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.*;

import static io.restassured.RestAssured.given;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TMDBTests {

    private static final String apiKey = "ff94f41b90c53f66a7708018f476010b";
    private static String token;
    private static String username = "dicruz";
    private static String password = "Testingintern2022";
    private static String sessionId;
    private static int listID;
    private int movieID = 512195;

    @BeforeClass
    public static void authenticate(){
        baseURI = "https://api.themoviedb.org/3";
        //Create request token
        token = given().params("api_key", apiKey)
                .when().get("/authentication/token/new")
                .then().statusCode(200).log().body()
                .and().extract().path("request_token");

        //Create session with login
        JSONObject user = new JSONObject();
        user.put("username", username);
        user.put("password", password);
        user.put("request_token", token);

        given().contentType(ContentType.JSON).accept(ContentType.JSON).body(user.toJSONString())
                .when().post("/authentication/token/validate_with_login"+"?api_key="+apiKey)
                .then().statusCode(200).log().body();

        JSONObject session = new JSONObject();
        session.put("request_token", token);

        //Create session
        sessionId = given().contentType(ContentType.JSON).accept(ContentType.JSON).body(session.toJSONString())
                .when().post("/authentication/session/new"+"?api_key="+apiKey)
                .then().statusCode(200).log().body()
                .and().extract().path("session_id");
    }

    @Test @Order(1)
    public void createList(){
        JSONObject list = new JSONObject();
        list.put("name", "Action movies 2");
        list.put("description", "The description");
        list.put("language", "en");

       listID = given().contentType(ContentType.JSON).accept(ContentType.JSON).body(list.toJSONString())
                .when().post("/list"+"?api_key="+apiKey+"&session_id="+sessionId)
                .then().statusCode(201).log().body()
                .and().extract().path("list_id");
    }

    @Test @Order(2)
    public void getListDetails(){
        given().params("api_key", apiKey)
                .when().get("/list/"+listID)
                .then().statusCode(200).log().body();
    }

    @Test @Order(3)
    public void addMovie(){
        JSONObject movieToAdd = new JSONObject();
        movieToAdd.put("media_id", movieID);

        given().contentType(ContentType.JSON).accept(ContentType.JSON).body(movieToAdd.toJSONString())
                .when().post("/list/"+listID+"/add_item"+"?api_key="+apiKey+"&session_id="+sessionId)
                .then().statusCode(201).log().body();
    }
    @Test @Order(4)
    public void getMovieDetails(){
        given().when().get("/movie/"+movieID+"?api_key="+apiKey)
                .then().statusCode(200).log().body();
    }

    @Test @Order(5)
    public void rateMovie(){
        JSONObject rateGiven = new JSONObject();
        rateGiven.put("value", 9);

        given().contentType(ContentType.JSON).accept(ContentType.JSON).body(rateGiven.toJSONString())
                .when().post("/movie/"+movieID+"/rating?api_key="+apiKey)
                .then().statusCode(201).log().body();
    }

    @Test @Order(6)
    public void clearList(){
        given().when().post("/list/"+listID+"/clear"+"?api_key="+apiKey+"&session_id="+sessionId+"&confirm="+true)
        //given().when().post("/list/8193679/clear"+"?api_key="+apiKey+"&session_id="+sessionId+"&confirm="+true)
                .then().statusCode(201).log().body();
    }

    @Test @Order(7)
    public void deleteList(){
        given().when().delete("/list/"+listID+"?api_key="+apiKey+"&session_id="+sessionId)
                .then().statusCode(201).log().body();
    }

}
