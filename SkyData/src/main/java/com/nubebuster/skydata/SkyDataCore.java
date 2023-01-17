package com.nubebuster.skydata;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class SkyDataCore {

    public static final String CHANNEL_NAME = "skynet";
    private static SkyDataCore inst;
    private final String redisHost;
    private final int redisPort;
    private final JedisPoolConfig poolConfig;
    private final RedisMessageStrategy redisMessageStrategy;
    public long lastKeepAlive = -1;
    private final JedisPubSub subscriptionHandler = new JedisPubSub() {
        @Override
        public void onMessage(String channel, String message) {
            lastKeepAlive = System.currentTimeMillis();
            redisMessageStrategy.run(message);
        }
    };
    private JedisPool jedisPool;
    private MYSQLDatabase database;
    private Jedis subscriberResource;

    private SkyDataCore(String redisHost, int redisPort, String mysqlHost, int mysqlPort,
                        String mysqlDatabase, String mysqlUser, String mysqlPassword, RedisMessageStrategy redisMessageStrategy) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(64);
        poolConfig.setMaxIdle(64);
        createPool();

        if (mysqlHost != null)
            database = new MYSQLDatabase(mysqlHost, mysqlPort, mysqlDatabase, mysqlUser, mysqlPassword);

        this.redisMessageStrategy = redisMessageStrategy;
    }

    protected static void initialize(String redisHost, int redisPort, String mysqlHost,
                                     int mysqlPort, String mysqlDatabase, String mysqlUser, String mysqlPassword, RedisMessageStrategy redisMessageStrategy) {
        inst = new SkyDataCore(redisHost, redisPort, mysqlHost, mysqlPort, mysqlDatabase, mysqlUser,
                mysqlPassword, redisMessageStrategy);
    }

    protected static SkyDataCore getInstance() {
        return inst;
    }

    /**
     * @return a mysql database object to execute updates and queries
     */
    public MYSQLDatabase getMYSQLDatabase() {
        return database;
    }

    public void subscribe() {
        subscribe(subscriptionHandler);
    }

    /**
     * This is a blocking function. Run asynchronously.
     */
    private void subscribe(JedisPubSub subscriber) {
        if (subscriberResource != null) {
            if (subscriber.isSubscribed()) {
                subscriber.unsubscribe();
            }
            subscriberResource.close();
        }
        subscriberResource = getJedisFromPool();
        subscriberResource.subscribe(subscriber, CHANNEL_NAME);
    }

    /**
     * Blocking operation. Run asynchronously.
     */
    public void setValue(String key, Object value, int secondsToExpire) {
        try (Jedis jedis = SkyDataCore.getInstance().getJedisFromPool()) {
            jedis.setex(key, secondsToExpire, String.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Blocking operation. Run asynchronously.
     */
    public void deleteValue(String key) {
        getJedisFromPool().del(key);
    }

    /**
     * @param key   is the reference key
     * @param value is the object. This will be converted to a string value.
     * @deprecated redis is generally used for caching. Consider {@link #setValue(String, Object, int) setValue}
     */
    @Deprecated
    public void setValuePermanently(String key, Object value) {
        try (Jedis jedis = SkyDataCore.getInstance().getJedisFromPool()) {
            jedis.set(key, String.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Blocking operation. Run asynchronously.
     */
    public String getValue(String key) {
        return getJedisFromPool().get(key);
    }

    /**
     * Blocking operation. Run asynchronously.
     */
    public void incrementValue(String key, int increment) {
        getJedisFromPool().incrBy(key, increment);
    }

    /**
     * Add a value(s) to the tail, the right side, of a list with the given key.
     * <p>
     * Blocking operation. Run asynchronously.
     */
    public void rPushValue(String key, String... value) {
        getJedisFromPool().rpush(key, value);
    }

    /**
     * Add a value(s) to the head, the left side, of a list with the given key.
     * <p>
     * Blocking operation. Run asynchronously.
     */
    public void lPushValue(String key, String... value) {
        getJedisFromPool().lpush(key, value);
    }

    /**
     * Get the value at the head, the left side, of a list with the given key and
     * remove the value from the list.
     * <p>
     * Blocking operation. Run asynchronously.
     */
    public String lPopValue(String key) {
        return getJedisFromPool().lpop(key);
    }

    /**
     * Get the value at the specified index of a list with the given key.
     * <p>
     * Blocking operation. Run asynchronously.
     */
    public String lIndexValue(String key, int index) {
        return getJedisFromPool().lindex(key, index);
    }

    /**
     * Get the value and remove the key
     * <p>
     * Blocking operation. Run asynchronously.
     */
    public String getDelValue(String key) {
        return getJedisFromPool().getDel(key);
    }

    /**
     * Locking operation. Run asynchronously.
     */
    public Jedis getJedisFromPool() {
        if (jedisPool.isClosed()) {
            createPool();
        }
        return jedisPool.getResource();
    }

    private void createPool() {
        jedisPool = new JedisPool(poolConfig, redisHost, redisPort, 60000);
    }

    /**
     * Sends a message to all servers on the network. Blocking operation! Run asynchronously or use the following instead
     * <p>{@link com.nubebuster.skydata.SkyDataBungee#publish(String)}  <p> {@link com.nubebuster.skydata.SkyDataSpigot#publish(String)}
     *
     * @param message payload to publish to all the servers on the network
     */
    public void publish(String message) {
        Jedis pub = getJedisFromPool();
        pub.publish(CHANNEL_NAME, message);
        pub.close();
    }

    public void cleanup() {
        if (subscriberResource != null)
            subscriberResource.close();
        if (jedisPool != null)
            jedisPool.close();
        database.closeConnection();
    }

}
