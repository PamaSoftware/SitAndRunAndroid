package software.pama.sitandrunandroid.run;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.appspot.formidable_code_826.sitAndRunApi.model.RunResultPiece;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import software.pama.sitandrunandroid.activities.helpers.IntentParams;
import software.pama.sitandrunandroid.integration.IntegrationLayer;
import software.pama.sitandrunandroid.integration.TheIntegrationLayer;
import software.pama.sitandrunandroid.integration.TheIntegrationLayerMock;
import software.pama.sitandrunandroid.model.RunResult;
import software.pama.sitandrunandroid.run.helpers.RunFinish;
import software.pama.sitandrunandroid.run.helpers.TheRunTimer;
import software.pama.sitandrunandroid.run.location.gps.GpsLocationListener;

// TODO sprawdzi� funkcjonalno�� czy ka�dy przypadek jest zawarty
// TODO testy, na przyk�ad czy wy��czane s� wszystkie listenery (logowanie zdarze�)
// TODO przeczyta� o logowaniu zdarze�
// TODO łapac wyjątki z watkow
public class ResultManager extends Service {

    private static final int FORECAST_SECONDS = 1;
    private static final int SLEEP_SECONDS = 2;
    private static final float BOARD_LINE_FOR_LOCATION = 5;
        private static final int LOCATION_ACCURACY_M = 70;
        private float locationUpdateDistanceM = 0;
//    private static final int LOCATION_ACCURACY_M = 20;
//    private float locationUpdateDistanceM = 30;
    private static final int HEAD = 0;
    //    private IntegrationLayer theIntegrationLayer = TheIntegrationLayerMock.getInstance();
    private IntegrationLayer theIntegrationLayer = TheIntegrationLayer.getInstance();
    private GpsLocationListener gpsLocationListener;
    private RunThread runThread;
    private float userDistance;
    private int distanceToRun;
    private Location userLocation;
    private RunFinish runFinish;
    private IBinder localBinder;
    private Handler threadHandler;
    private List<RunResult> userResults = new CopyOnWriteArrayList<>();
    private List<RunResult> enemyResults = new CopyOnWriteArrayList<>();
    private TheRunTimer theRunTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        localBinder = new LocalBinder();
        threadHandler = new Handler();
        theRunTimer = TheRunTimer.getInstance();
        runFinish = new RunFinish();
        // testowo
//        userDistance = -10;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        distanceToRun = intent.getIntExtra(IntentParams.DISTANCE_TO_RUN, -1);
        gpsLocationListener = new GpsLocationListener(this);
        gpsLocationListener.startLocationListener();
        Toast.makeText(this, "GPSLocationListener started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    public void startRun() {
        theRunTimer.start();
        runThread = new RunThread();
        threadHandler.post(runThread);
        new GetEnemyResult().execute();
    }

    /**
     * Pozycję użytkownika wyświetlam tylko jako wyliczony z GPS-a dystans.
     * Jeżeli przeciwnik się zatrzyma, a ja będę dalej przewidywał pozycję, będę robił to błędnie
     * Powinienem sprawdzać czy użytkownik biegnie (krokomierzem), jeśli tak to mogę przewidzieć pozcyję użytkownika,
     * Na razie zwracam najnowszy wynik.
     */
    public RunResult getUserResult() {
        int resultsSize = userResults.size();
        if (resultsSize == 0) {
            return new RunResult(0, theRunTimer.getRunDurationMillis());
        }
        else if (resultsSize > 0) {
            RunResult latestResult = userResults.get(HEAD);
            RunResult nextResult;
            if (userResults.size() == 1)
                nextResult = new RunResult(0, 0);
            else
                nextResult = userResults.get(1);
            float s1 = nextResult.getTotalDistance();
            float s2 = latestResult.getTotalDistance();
            long t1 = nextResult.getTotalTimeMillis();
            long t2 = latestResult.getTotalTimeMillis();
            long t3 = theRunTimer.getRunDurationMillis(); //t3
            float distanceForTheMoment;
            if (t3 < t2) {
                throw new RuntimeException();
            } else {
                float deltaDistance = ((s2 - s1)/(t2 - t1))*(t3 - t2);
                distanceForTheMoment = s2 + deltaDistance;
            }
            if (distanceForTheMoment >= distanceToRun) {
                return new RunResult(distanceToRun, t3);
            }
            distanceForTheMoment = roundUp((int) distanceForTheMoment);
            return new RunResult(distanceForTheMoment, t3);
        }
//        } else if (resultsSize == 1) {
//            RunResult result = enemyResults.get(HEAD);
//            RunResult prediction = predictWithOneResult(result);
//            return prediction;
//        } else {
//            RunResult latestResult = userResults.get(HEAD);
//            RunResult nextResult = userResults.get(1);
//            RunResult prediction = predictWithTwoResults(latestResult, nextResult);
////            Logger.getLogger("").info("User distance = " + distanceForTheMoment);
//            if (prediction.getTotalDistance() >= distanceToRun)
//                return new RunResult(distanceToRun, prediction.getTotalTimeMillis());
//            return prediction;
//        }
        throw new RuntimeException();
//        return userResults.get(HEAD);
    }

    /**
     * Jeśli mamy jeden wynik, to dopisujemy start jako wynik i wyliczamy pozycję pomiędzy biegami.
     * Jeśli jesteśmy pomiędzy aktualnym czasem, a przewidywaniem, to oznacza że możemy przeliczać pozycję.
     * Jeśli jesteśmy po przewidywaniu, możemy błędnie podawać wynik, bo nie wiemy czy przeciwnik biegnie
     * czy stoi, dlatego lepiej wydłużyc forecast. Ale jak duży będzie forecast, to możemy doprowadzić do
     * sytuacji w której serwer przewiduje pozycję przeciwnika, a jak dostaje poprawną to my musimy cofać się
     * z obliczeniami. Daltego najlepiej wyliczać dystans pomiędzy poprzednim wynikiem, a przewidywaniem, natomiast
     * jeśli chcemy wyświetlić wynik później to podobnie jak w przypadku użytkownika powinniśmy zwrócić jego
     * ostatnią pozycję, a nie wykonywać jakieś dziwne obliczenia.
     */
    public RunResult getEnemyResult() {
        int resultsSize = enemyResults.size();
        if (resultsSize == 0) {
            return new RunResult(0, theRunTimer.getRunDurationMillis());
        }
        else if (resultsSize > 0) {
            RunResult latestResult = enemyResults.get(HEAD);
            RunResult nextResult;
            if (enemyResults.size() == 1)
                nextResult = new RunResult(0, 0);
            else
                nextResult = enemyResults.get(1);
            float s1 = nextResult.getTotalDistance();
            float s2 = latestResult.getTotalDistance();
            long t1 = nextResult.getTotalTimeMillis();
            long t2 = latestResult.getTotalTimeMillis();
            long currentRunTime = theRunTimer.getRunDurationMillis(); //t3
            float distanceForTheMoment;
            if (currentRunTime < t2) {
                float deltaDistance = ((s2 - s1)/(t2 - t1))*(currentRunTime - t1);
                distanceForTheMoment = s1 + deltaDistance;
            } else {
                float deltaDistance = ((s2 - s1)/(t2 - t1))*(currentRunTime - t2);
                distanceForTheMoment = s2 + deltaDistance;
            }
            if (distanceForTheMoment >= distanceToRun) {
                return new RunResult(distanceToRun, currentRunTime);
            }
            distanceForTheMoment = roundUp((int) distanceForTheMoment);
            return new RunResult(distanceForTheMoment, currentRunTime);
        }
        throw new RuntimeException();
    }

    private int roundUp(int n) {
        return (n + 4) / 5 * 5;
    }

    public long getRunTime() {
        return theRunTimer.getRunDurationMillis();
    }

    public void stopRun() {
        destroy();
    }

    private void destroy() {
        gpsLocationListener.stop();
        runFinish.setRunOver(true);
        stopSelf();
    }

    public RunFinish getRunFinish() {
       return runFinish;
    }

    private void onRunFinished() {
        runFinish.setRunOver(true);
        gpsLocationListener.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy();
    }

    private RunResult predictWithOneResult(RunResult result) {
        long currentRunTime = theRunTimer.getRunDurationMillis(); // t1
        float speed = result.getTotalDistance()/result.getTotalTimeMillis();
        float distanceForTheMoment = speed*currentRunTime;
        return new RunResult(distanceForTheMoment, currentRunTime);
    }

    private RunResult predictWithTwoResults(RunResult latestResult, RunResult olderResult) {
        float distanceDiff = latestResult.getTotalDistance() - olderResult.getTotalDistance();
        long timeDiff = latestResult.getTotalTimeMillis() - olderResult.getTotalTimeMillis();
        float speed = distanceDiff/timeDiff;
        long currentRunTime = theRunTimer.getRunDurationMillis(); // t1
        long deltaTime = currentRunTime - latestResult.getTotalTimeMillis();
        float distanceForTheMoment = latestResult.getTotalDistance() + speed*deltaTime;
        return new RunResult(distanceForTheMoment, currentRunTime);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        public ResultManager getUserResultManager() {
            return ResultManager.this;
        }
    }

    private class RunThread extends Thread {

        @Override
        public void run() {
            // TODO mozliwe ze trzeba bedzie zmienic z ilosci satelit na dokladnosc z jaka pobierana byla seria ostatnich wynikow
//            if (false && gpsLocationListener.getFixedSatellitesNumber() < 5 && userResults.size() > 0) {
////                // TODO przetestować to i ujednolicic, bo jest to taki sam algorytm prewidywania jak w prypadku zwracania biezacego wyniku
////                // TODO dokladnie to samo bedzie w przypadku przeciwnika, tylko tam sprawdzamy
//                if (userResults.size() == 1) {
//                    RunResult prediction = predictWithOneResult(userResults.get(HEAD));
//                    userResults.add(HEAD, prediction);
//                    userDistance = prediction.getTotalDistance();
//                }
//                else if (userResults.size() >= 2) {
//                    RunResult latestResult = userResults.get(HEAD);
//                    int edge;
//                    if (userResults.size() >= 5)
//                        edge = 5;
//                    else
//                        edge = userResults.size();
//                    RunResult edgeResult = userResults.get(edge - 1);
//                    RunResult prediction = predictWithTwoResults(latestResult, edgeResult);
//                    userResults.add(HEAD, prediction);
//                    userDistance = prediction.getTotalDistance();
//                }
//            }
//            else {

                userDistance += 5;
                if (userDistance > 0)
                    userResults.add(HEAD, new RunResult(userDistance, theRunTimer.getRunDurationMillis()));

//                Location newLocation = gpsLocationListener.getCurrentLocation();
//                if (newLocation != null && isAccurate(newLocation) && isFarEnough(newLocation)) {
//                    if (userLocation != null)
//                        userDistance += userLocation.distanceTo(newLocation);
//                    userLocation = newLocation;
//                    userResults.add(HEAD, new RunResult(userDistance, theRunTimer.getRunDurationMillis()));
//                    float edge = (distanceToRun - userDistance) / (float) distanceToRun;
//                    if (edge < 0.05) {
//                        lowerLocationUpdateDistance(0.33f);
//                    }
//                    Log.d("RunThread", "Total distance: " + userDistance);
//                } else {
//                    // TODO to był kiedyś problem, teraz nie wiadomo jak to wpłynie na przebieg biegu, powinno być wszystko okej
//                    userResults.add(HEAD, new RunResult(userDistance, theRunTimer.getRunDurationMillis()));
//                }
//            }
            Logger.getLogger("").info("Run " + userDistance);
            if (distanceToRun - userDistance <= 0 || runFinish.isRunOver()) {
                Logger.getLogger("").info("Stopping run thread.");
                gpsLocationListener.stop();
                return;
            }
            threadHandler.postDelayed(this, 1000);
        }

        private boolean isAccurate(Location location) {
            if (location == null)
                return false;
            Log.d(getClass().getSimpleName(), "Location accuracy: " + location.getAccuracy());
            return location.getAccuracy() > 0.0 && location.getAccuracy() <= LOCATION_ACCURACY_M;
        }

        private boolean isFarEnough(Location location) {
            if (location == null)
                return false;
            if (userLocation == null)
                return true;
            float distance = userLocation.distanceTo(location);
            Log.d(getClass().getSimpleName(), "Location far enough: " + distance);
            return distance > locationUpdateDistanceM;
        }

        public void lowerLocationUpdateDistance(float lowerPercentage) {
            float result = locationUpdateDistanceM - locationUpdateDistanceM * lowerPercentage;
            if (result >= BOARD_LINE_FOR_LOCATION) {
                locationUpdateDistanceM = result;
                Log.i(getClass().getSimpleName(), "Location update distance changed to " + result);
            }
        }
    }

    private class GetEnemyResult extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RunResult prevResult = null;
            while (!runFinish.isRunOver())
                prevResult = updateUserAndGetEnemyResult(prevResult);
            return null;
        }

        private RunResult updateUserAndGetEnemyResult(RunResult prevResult) {
            // sprawdzić czy jest połączenie, lub w wyjatku zrobic obsluge, ale na poczatku lepiej tutaj
            // jesli nie ma, to zwracam usredniony wynik z historii
            // jesli jest to lacze sie z siecią i pobieram wynik, pewnie lepiej w wyjatku bo tutaj moze
            // byc siec, a cos moze pojsc nie tak
            RunResult userResult;
            if (userResults.isEmpty())
                userResult = new RunResult(0, theRunTimer.getRunDurationMillis());
            else
                userResult = userResults.get(HEAD);
            try {
                if (userResult.greaterThan(prevResult)) {
                    Logger.getAnonymousLogger().log(Level.INFO, "Moj czas " + userResult.getTotalTimeMillis() + " dystans " + userResult.getTotalDistance());
                    RunResultPiece enemyResultPiece = theIntegrationLayer.getEnemyResult(FORECAST_SECONDS, userResult);
                    if (enemyResultPiece != null) {
                        Integer enemyDistance = enemyResultPiece.getDistance();
                        if (enemyDistance > 0)
                            enemyResults.add(HEAD, new RunResult(enemyDistance, enemyResultPiece.getTime() * 1000));
                        else if (enemyDistance == -1 && enemyResultPiece.getTime() >= 0) {
                            runFinish.setRunOver(true);
                            if (enemyResultPiece.getTime() == 0) {
                                enemyResults.add(HEAD, new RunResult(distanceToRun, theRunTimer.getRunDurationMillis()));
                                Logger.getAnonymousLogger().log(Level.INFO, "Enemy won");
                                runFinish.setUserWon(false);
                            }
                            else if (enemyResultPiece.getTime() == 1) {
                                Logger.getAnonymousLogger().log(Level.INFO, "User won");
                                runFinish.setUserWon(true);
                            }
                        }
                        Logger.getAnonymousLogger().log(Level.INFO, "Dystans przeciwnika: " + enemyDistance);
                        prevResult = userResult;
                    }
                }
                Thread.sleep(1000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return prevResult;
        }

    }

}
