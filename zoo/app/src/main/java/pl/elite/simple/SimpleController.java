package pl.elite.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SimpleController {

    private final SimpleService service;
    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);

    @Autowired
    public SimpleController(SimpleService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "dziala";
    }

    @GetMapping("/list")
    public List<String> listPath(@RequestParam(name = "path", defaultValue = "/") String path) {
        return service.getChildren(path);
    }

    @GetMapping("/get")
    public String get(@RequestParam(name = "path") String path) {
        return service.get(path);
    }

    @PostMapping("/set")
    public String set(@RequestParam(name = "path") String path, @RequestBody String value) {
        if (service.set(path, value)) {
            return "ok";
        } else {
            return "oh no! POP!!! <<by lemming>>";
        }
    }

    @PostMapping("/setWithTtl")
    public String setWithTtl(
        @RequestParam(name = "path") String path, @RequestParam(name = "ttl") Long ttl, @RequestBody String value
    ) {
        if (service.setWithTTL(path, value, ttl)) {
            return "ok";
        } else {
            return "oh no! POP!!! <<by lemming>>";
        }
    }

    @GetMapping("/useLock")
    public String setWithLock(
        @RequestParam(name = "lockTime") Long lockTime,
        @RequestParam(name = "waitTime") Long waitTime
    ) {
        if (service.setWithLock(lockTime, waitTime)) {
            return "ok";
        } else {
            return "oh no! POP!!! <<by lemming>>";
        }
    }

    @PostMapping("/setWithAcl")
    public String setWithAcl(@RequestParam(name = "path") String path, @RequestBody String value) {
        if (service.setWithACL(path, value)) {
            return "ok";
        } else {
            return "oh no! POP!!! <<by lemming>>";
        }
    }

}
