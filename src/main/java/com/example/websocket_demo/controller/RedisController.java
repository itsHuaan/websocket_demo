package com.example.websocket_demo.controller;

import com.example.websocket_demo.common.MessageService;
import static com.example.websocket_demo.enumeration.ResponseMessage.*;
import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.service.redis.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A small REST surface over Redis so the store can be inspected and edited without
 * the redis-cli. All operations are delegated to {@link RedisService}; this class only
 * maps HTTP requests and wraps results in {@link ApiResponse}.
 *
 * <p>Sits under {@code /v1/api/redis} which is not whitelisted, so all endpoints
 * require an authenticated request. The destructive ones (delete, flush) are powerful;
 * lock this controller to an admin role if it is ever exposed beyond local/dev use.
 */
@RestController
@Tag(name = "Redis Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/redis")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class RedisController {
    MessageService messageService;

    RedisService redisService;

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK, message, data));
    }

    // ===================== Keys / generic =====================

    @Operation(summary = "List keys matching a glob pattern (default *)")
    @GetMapping("/keys")
    public ResponseEntity<ApiResponse<Set<String>>> keys(@RequestParam(defaultValue = "*") String pattern) {
        return ok("Keys fetched", redisService.keys(pattern));
    }

    @Operation(summary = "Number of keys in the current database")
    @GetMapping("/dbsize")
    public ResponseEntity<ApiResponse<Long>> dbSize() {
        return ok("Key count", redisService.dbSize());
    }

    @Operation(summary = "Whether a key exists")
    @GetMapping("/exists/{key}")
    public ResponseEntity<ApiResponse<Boolean>> exists(@PathVariable String key) {
        return ok("Existence checked", redisService.exists(key));
    }

    @Operation(summary = "Data type of a key (string, list, set, zset, hash, none)")
    @GetMapping("/type/{key}")
    public ResponseEntity<ApiResponse<String>> type(@PathVariable String key) {
        return ok("Type fetched", redisService.type(key));
    }

    @Operation(summary = "Remaining time-to-live of a key, in seconds (-1 = no expiry, -2 = missing)")
    @GetMapping("/ttl/{key}")
    public ResponseEntity<ApiResponse<Long>> ttl(@PathVariable String key) {
        return ok("TTL fetched", redisService.ttl(key));
    }

    @Operation(summary = "Set a key's expiry, in seconds")
    @PostMapping("/expire/{key}")
    public ResponseEntity<ApiResponse<Boolean>> expire(@PathVariable String key, @RequestParam long seconds) {
        return ok("Expiry set", redisService.expire(key, seconds));
    }

    @Operation(summary = "Remove a key's expiry (make it persistent)")
    @PostMapping("/persist/{key}")
    public ResponseEntity<ApiResponse<Boolean>> persist(@PathVariable String key) {
        return ok("Persist applied", redisService.persist(key));
    }

    @Operation(summary = "Rename a key")
    @PostMapping("/rename")
    public ResponseEntity<ApiResponse<Void>> rename(@RequestParam String key, @RequestParam String newKey) {
        if (!redisService.rename(key, newKey)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND, messageService.getMessage(KEY.getCode()) + key + "' does not exist"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(KEY_RENAMED.getCode())));
    }

    @Operation(summary = "Delete a single key")
    @DeleteMapping("/keys/{key}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable String key) {
        return ok("Key deleted", redisService.delete(key));
    }

    @Operation(summary = "Delete every key matching a glob pattern; returns how many were removed")
    @DeleteMapping("/keys")
    public ResponseEntity<ApiResponse<Long>> deleteByPattern(@RequestParam String pattern) {
        return ok("Keys deleted", redisService.deleteByPattern(pattern));
    }

    @Operation(summary = "Flush the entire current database (irreversible)")
    @DeleteMapping("/flush")
    public ResponseEntity<ApiResponse<Void>> flush() {
        redisService.flush();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(DATABASE_FLUSHED.getCode())));
    }

    // ===================== String values =====================

    @Operation(summary = "Get a string value")
    @GetMapping("/string/{key}")
    public ResponseEntity<ApiResponse<String>> getString(@PathVariable String key) {
        return ok("Value fetched", redisService.getString(key));
    }

    @Operation(summary = "Set a string value, optionally with a TTL in seconds")
    @PostMapping("/string/{key}")
    public ResponseEntity<ApiResponse<Void>> setString(@PathVariable String key,
                                                       @RequestBody(required = false) String value,
                                                       @RequestParam(required = false) Long ttlSeconds) {
        redisService.setString(key, value, ttlSeconds);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(VALUE_SET.getCode())));
    }

    @Operation(summary = "Increment a numeric string value (default by 1); returns the new value")
    @PostMapping("/string/{key}/increment")
    public ResponseEntity<ApiResponse<Long>> increment(@PathVariable String key,
                                                       @RequestParam(defaultValue = "1") long by) {
        return ok("Value incremented", redisService.increment(key, by));
    }

    // ===================== Hashes =====================

    @Operation(summary = "Get all fields and values of a hash")
    @GetMapping("/hash/{key}")
    public ResponseEntity<ApiResponse<Map<Object, Object>>> getHash(@PathVariable String key) {
        return ok("Hash fetched", redisService.getHash(key));
    }

    @Operation(summary = "Get a single hash field")
    @GetMapping("/hash/{key}/{field}")
    public ResponseEntity<ApiResponse<Object>> getHashField(@PathVariable String key, @PathVariable String field) {
        return ok("Field fetched", redisService.getHashField(key, field));
    }

    @Operation(summary = "Set a hash field")
    @PostMapping("/hash/{key}/{field}")
    public ResponseEntity<ApiResponse<Void>> setHashField(@PathVariable String key, @PathVariable String field,
                                                         @RequestBody(required = false) String value) {
        redisService.setHashField(key, field, value);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(FIELD_SET.getCode())));
    }

    @Operation(summary = "Delete a hash field; returns how many were removed")
    @DeleteMapping("/hash/{key}/{field}")
    public ResponseEntity<ApiResponse<Long>> deleteHashField(@PathVariable String key, @PathVariable String field) {
        return ok("Field deleted", redisService.deleteHashField(key, field));
    }

    // ===================== Lists =====================

    @Operation(summary = "Get all elements of a list")
    @GetMapping("/list/{key}")
    public ResponseEntity<ApiResponse<List<String>>> getList(@PathVariable String key) {
        return ok("List fetched", redisService.getList(key));
    }

    @Operation(summary = "Push a value onto a list (end = left or right); returns the new length")
    @PostMapping("/list/{key}")
    public ResponseEntity<ApiResponse<Long>> pushList(@PathVariable String key,
                                                      @RequestParam(defaultValue = "right") String end,
                                                      @RequestBody(required = false) String value) {
        return ok("Value pushed", redisService.pushList(key, end, value));
    }

    @Operation(summary = "Pop a value off a list (end = left or right)")
    @DeleteMapping("/list/{key}/pop")
    public ResponseEntity<ApiResponse<String>> popList(@PathVariable String key,
                                                       @RequestParam(defaultValue = "left") String end) {
        return ok("Value popped", redisService.popList(key, end));
    }

    // ===================== Sets =====================

    @Operation(summary = "Get all members of a set")
    @GetMapping("/set/{key}")
    public ResponseEntity<ApiResponse<Set<String>>> getSet(@PathVariable String key) {
        return ok("Set fetched", redisService.getSet(key));
    }

    @Operation(summary = "Add a member to a set; returns how many were newly added")
    @PostMapping("/set/{key}")
    public ResponseEntity<ApiResponse<Long>> addToSet(@PathVariable String key,
                                                      @RequestBody(required = false) String member) {
        return ok("Member added", redisService.addToSet(key, member));
    }

    @Operation(summary = "Remove a member from a set; returns how many were removed")
    @DeleteMapping("/set/{key}/{member}")
    public ResponseEntity<ApiResponse<Long>> removeFromSet(@PathVariable String key, @PathVariable String member) {
        return ok("Member removed", redisService.removeFromSet(key, member));
    }

    // ===================== Sorted sets =====================

    @Operation(summary = "Get all members of a sorted set with their scores, lowest first")
    @GetMapping("/zset/{key}")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getZSet(@PathVariable String key) {
        return ok("Sorted set fetched", redisService.getZSet(key));
    }

    @Operation(summary = "Add (or re-score) a member in a sorted set")
    @PostMapping("/zset/{key}")
    public ResponseEntity<ApiResponse<Boolean>> addToZSet(@PathVariable String key, @RequestParam double score,
                                                          @RequestBody(required = false) String member) {
        return ok("Member added", redisService.addToZSet(key, score, member));
    }

    @Operation(summary = "Remove a member from a sorted set; returns how many were removed")
    @DeleteMapping("/zset/{key}/{member}")
    public ResponseEntity<ApiResponse<Long>> removeFromZSet(@PathVariable String key, @PathVariable String member) {
        return ok("Member removed", redisService.removeFromZSet(key, member));
    }
}