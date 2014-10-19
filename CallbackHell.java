import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;

/**
 * This is how callback hell looks like...
 *
 * @author Reza Rahman
 */
@Stateless
public class CallbackHell {

    @Resource
    private ManagedExecutorService executor;

    public void underwriteInsured(Person person, Consumer<Coverage> callback) {
        try {
            final CountDownLatch outerLatch = new CountDownLatch(2);
            final AtomicReference<Credit> credit = new AtomicReference<>();
            final AtomicReference<Health> health = new AtomicReference<>();

            executor.execute(() -> {
                try {
                    final CountDownLatch innerLatch = new CountDownLatch(2);
                    final AtomicReference<Assets> assets = new AtomicReference<>();
                    final AtomicReference<Liabilities> liabilities = new AtomicReference<>();

                    executor.execute(() -> {
                        getAssets(person, (a) -> {
                            assets.set(a);
                            innerLatch.countDown();
                        });
                    });

                    executor.execute(() -> {
                        getLiabilities(person, (l) -> {
                            liabilities.set(l);
                            innerLatch.countDown();
                        });
                    });

                    // Wait for remote data to be available.
                    innerLatch.await();
                    calculateCreditScore(assets.get(), liabilities.get(), (c) -> {
                        credit.set(c);
                        outerLatch.countDown();
                    });
                } catch (InterruptedException ex) {
                    throw new RuntimeException("Error calculating credit score", ex);
                }
            });

            executor.execute(() -> {
                getHealthHistory(person, (history) -> {
                    calculateHeathScore(history, (h) -> {
                        health.set(h);
                        outerLatch.countDown();
                    });
                });
            });

            // Wait for remote data to be available.
            outerLatch.await();
            executor.execute(() -> underwrite(credit.get(), health.get(), callback));
        } catch (InterruptedException ex) {
            throw new RuntimeException("Error calculating coverage", ex);
        }
    }

    private void getAssets(Person person, Consumer<Assets> callback) {
        Assets assets = null;
        // Get person's assets from remote source.
        callback.accept(assets);
    }

    private void getLiabilities(Person person, Consumer<Liabilities> callback) {
        Liabilities liabilities = null;
        // Get person's liabilities from remote source.
        callback.accept(liabilities);
    }

    private void calculateCreditScore(Assets assets, Liabilities liabilities,
            Consumer<Credit> callback) {
        Credit credit = null;
        // Lengthy credit score calculation.
        callback.accept(credit);
    }

    private void getHealthHistory(Person person, Consumer<History> callback) {
        History history = null;
        // Get person's health history from remote source.
        callback.accept(history);
    }

    private void calculateHeathScore(History history, Consumer<Health> callback) {
        Health health = null;
        // Get person's health history from remote source.
        callback.accept(health);
    }

    private void underwrite(Credit credit, Health health, Consumer<Coverage> callback) {
        Coverage coverage = null;
        // Lengthy underwriting calculation.
        callback.accept(coverage);
    }
}
