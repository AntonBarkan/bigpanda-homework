package homework;


import io.reactivex.subjects.BehaviorSubject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class WorkService {

    @Value("${execution.path}")
    public String executionPath;

    @Async
    public void runWorker(BehaviorSubject<String> lines) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(executionPath);
        Process p = pb.start();
        try(BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                lines.onNext(line);
            }
        }
    }
}
