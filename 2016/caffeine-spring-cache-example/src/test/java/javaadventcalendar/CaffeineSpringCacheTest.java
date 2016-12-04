package javaadventcalendar;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class CaffeineSpringCacheTest {
    @Autowired
    CalcService calcService;

    void sleep(long sleepSec) {
        try {
            TimeUnit.SECONDS.sleep(sleepSec);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    // stop-watch
    long sw(Runnable runnable) {
        long startTime = System.nanoTime();
        runnable.run();
        long elapsedTime = System.nanoTime() - startTime;

        return TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
    }

    @Test
    public void gettingStarted() {
        // 1回目は低速
        assertThat(
                sw(() -> assertThat(calcService.add(1, 2)).isEqualTo(3))
        ).isGreaterThanOrEqualTo(5L);

        // 2回目は高速
        assertThat(
                sw(() -> assertThat(calcService.add(1, 2)).isEqualTo(3))
        ).isLessThan(1L);

        // 有効期限切れを待つ
        sleep(10L);

        // 低速に戻る
        assertThat(
                sw(() -> assertThat(calcService.add(1, 2)).isEqualTo(3))
        ).isGreaterThanOrEqualTo(5L);
    }
}
