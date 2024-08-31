package com.camila.api.product.framework.adapter.input.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The type Product web socket handler it case.
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("[IT][ProductWebSocketHandler] Product websocket handler test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductWebSocketHandlerITCase {

  private static WebSocketClient webSocketClient;

  @Autowired
  private ObjectMapper objectMapper;

  @LocalServerPort
  private int randomPort;

  private URI uri;

  /**
   * Before all.
   */
  @BeforeAll
  static void beforeAll() {
    webSocketClient = new ReactorNettyWebSocketClient();
  }

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    assertNotNull(objectMapper);
    uri = URI.create("ws://localhost:" + randomPort + "/product-dev/api/ws/products");
  }

  /**
   * Handle find by internal id ok.
   *
   * @throws URISyntaxException the uri syntax exception
   */
  @Test
  @DisplayName("[ProductWebSocketHandler] handle find by internal id - Ok")
  @Order(3)
  void handleFindByInternalIdOk() throws URISyntaxException {
    String findByInternalIdRequest = """
      {
        "method": "FIND_BY_INTERNAL_ID",
        "internalId": "1"
      }
      """;

    webSocketClient.execute(uri,
        session -> session.send(
          Mono.just(session.textMessage(findByInternalIdRequest))
        ).thenMany(session.receive()
          .take(1)
          .map(WebSocketMessage::getPayloadAsText)
        ).flatMap(message -> {
          log.info("Received findByInternalI: {}", message);
          try {
            var jsonNode = objectMapper.readTree(message);
            assertEquals( 1, jsonNode.get("internalId").asInt());
            assertEquals( "SHIRT", jsonNode.get("category").asText());
            assertEquals( "V-NECH BASIC SHIRT", jsonNode.get("name").asText());
          } catch (JsonProcessingException e) {
            log.error("Error parsing json", e);
          }
          return Mono.empty();
        }).then())
      .block(Duration.ofMillis(5000L));
  }

  /**
   * Test sort products ok.
   */
  @Test
  @DisplayName("[ProductWebSocketHandler] handle sort products - Ok")
  @Order(3)
  void testSortProductsOk() {
    String sortProductsRequest = """
      {
        "method": "SORT_PRODUCTS",
        "salesUnits": "0.8",
        "stock": "0.2",
        "page": "0",
        "size": "10"
      }
      """;

    webSocketClient.execute(uri,
        session -> session.send(
          Mono.just(session.textMessage(sortProductsRequest))
        ).thenMany(session.receive()
          .take(1)
          .map(WebSocketMessage::getPayloadAsText)
        ).flatMap(message -> {
          log.info("Received sortProducts: {}", message);
          try {
            var jsonNode = objectMapper.readTree(message);
            assertEquals( 5, jsonNode.get("internalId").asInt());
            assertEquals( "SHIRT", jsonNode.get("category").asText());
            assertEquals( "CONTRASTING LACE T-SHIRT", jsonNode.get("name").asText());
          } catch (JsonProcessingException e) {
            log.error("Error parsing json", e);
          }
          return Mono.empty();
        }).then())
      .block(Duration.ofMillis(5000L));
  }
}