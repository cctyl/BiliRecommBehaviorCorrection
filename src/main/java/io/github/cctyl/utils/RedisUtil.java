package io.github.cctyl.utils;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public RedisUtil() {
    }

    public void delete(String key) {
        this.redisTemplate.delete(key);
    }

    public void delete(Collection<String> keys) {
        this.redisTemplate.delete(keys);
    }

    public byte[] dump(String key) {
        return this.redisTemplate.dump(key);
    }

    public Boolean hasKey(String key) {
        return this.redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return this.redisTemplate.expire(key, timeout, unit);
    }

    public Boolean expireAt(String key, Date date) {
        return this.redisTemplate.expireAt(key, date);
    }

    public Set<String> keys(String pattern) {
        return this.redisTemplate.keys(pattern);
    }

    public Boolean move(String key, int dbIndex) {
        return this.redisTemplate.move(key, dbIndex);
    }

    public Boolean persist(String key) {
        return this.redisTemplate.persist(key);
    }

    public Long getExpire(String key, TimeUnit unit) {
        return this.redisTemplate.getExpire(key, unit);
    }

    public Long getExpire(String key) {
        return this.redisTemplate.getExpire(key);
    }

    public String randomKey() {
        return (String) this.redisTemplate.randomKey();
    }

    public void rename(String oldKey, String newKey) {
        this.redisTemplate.rename(oldKey, newKey);
    }

    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return this.redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    public DataType type(String key) {
        return this.redisTemplate.type(key);
    }

    public void set(String key, Object value) {
        this.redisTemplate.opsForValue().set(key, value);
    }

    public Object get(String key) {
        return this.redisTemplate.opsForValue().get(key);
    }

    public String getRange(String key, long start, long end) {
        return this.redisTemplate.opsForValue().get(key, start, end);
    }

    public String getAndSet(String key, Object value) {
        return (String) this.redisTemplate.opsForValue().getAndSet(key, value);
    }

    public Boolean getBit(String key, long offset) {
        return this.redisTemplate.opsForValue().getBit(key, offset);
    }

    public List<Object> multiGet(Collection<String> keys) {
        return this.redisTemplate.opsForValue().multiGet(keys);
    }

    public boolean setBit(String key, long offset, boolean value) {
        return this.redisTemplate.opsForValue().setBit(key, offset, value);
    }

    public void setEx(String key, Object value, long timeout, TimeUnit unit) {
        this.redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public boolean setIfAbsent(String key, Object value) {
        return this.redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public void setRange(String key, Object value, long offset) {
        this.redisTemplate.opsForValue().set(key, value, offset);
    }

    public Long size(String key) {
        return this.redisTemplate.opsForValue().size(key);
    }

    public void multiSet(Map<String, String> maps) {
        this.redisTemplate.opsForValue().multiSet(maps);
    }

    public boolean multiSetIfAbsent(Map<String, String> maps) {
        return this.redisTemplate.opsForValue().multiSetIfAbsent(maps);
    }

    public Long incrBy(String key, long increment) {
        return this.redisTemplate.opsForValue().increment(key, increment);
    }

    public Double incrByFloat(String key, double increment) {
        return this.redisTemplate.opsForValue().increment(key, increment);
    }

    public Integer append(String key, String value) {
        return this.redisTemplate.opsForValue().append(key, value);
    }

    public Object hGet(String key, String field) {
        return this.redisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> hGetAll(String key) {
        return this.redisTemplate.opsForHash().entries(key);
    }

    public List<Object> hMultiGet(String key, Collection<Object> fields) {
        return this.redisTemplate.opsForHash().multiGet(key, fields);
    }

    public void hPut(String key, String hashKey, String value) {
        this.redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public void hPutAll(String key, Map<String, String> maps) {
        this.redisTemplate.opsForHash().putAll(key, maps);
    }

    public Boolean hPutIfAbsent(String key, String hashKey, String value) {
        return this.redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }

    public Long hDelete(String key, Object... fields) {
        return this.redisTemplate.opsForHash().delete(key, fields);
    }

    public boolean hExists(String key, String field) {
        return this.redisTemplate.opsForHash().hasKey(key, field);
    }

    public Long hIncrBy(String key, Object field, long increment) {
        return this.redisTemplate.opsForHash().increment(key, field, increment);
    }

    public Double hIncrByFloat(String key, Object field, double delta) {
        return this.redisTemplate.opsForHash().increment(key, field, delta);
    }

    public Set<Object> hKeys(String key) {
        return this.redisTemplate.opsForHash().keys(key);
    }

    public Long hSize(String key) {
        return this.redisTemplate.opsForHash().size(key);
    }

    public List<Object> hValues(String key) {
        return this.redisTemplate.opsForHash().values(key);
    }

    public Cursor<Entry<Object, Object>> hScan(String key, ScanOptions options) {
        return this.redisTemplate.opsForHash().scan(key, options);
    }

    public Object lIndex(String key, long index) {
        return this.redisTemplate.opsForList().index(key, index);
    }

    public List<Object> lRange(String key, long start, long end) {
        return this.redisTemplate.opsForList().range(key, start, end);
    }

    public Long lLeftPush(String key, String value) {
        return this.redisTemplate.opsForList().leftPush(key, value);
    }

    public Long lLeftPushAll(String key, String... value) {
        return this.redisTemplate.opsForList().leftPushAll(key, value);
    }

    public Long lLeftPushAll(String key, Collection<String> value) {
        return this.redisTemplate.opsForList().leftPushAll(key, new Object[]{value});
    }

    public Long lLeftPushIfPresent(String key, String value) {
        return this.redisTemplate.opsForList().leftPushIfPresent(key, value);
    }

    public Long lLeftPush(String key, String pivot, String value) {
        return this.redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    public Long lRightPush(String key, String value) {
        return this.redisTemplate.opsForList().rightPush(key, value);
    }

    public Long lRightPushAll(String key, String... value) {
        return this.redisTemplate.opsForList().rightPushAll(key, value);
    }

    public Long lRightPushAll(String key, Collection<String> value) {
        return this.redisTemplate.opsForList().rightPushAll(key, new Object[]{value});
    }

    public Long lRightPushIfPresent(String key, String value) {
        return this.redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    public Long lRightPush(String key, String pivot, String value) {
        return this.redisTemplate.opsForList().rightPush(key, pivot, value);
    }

    public void lSet(String key, long index, String value) {
        this.redisTemplate.opsForList().set(key, index, value);
    }

    public Object lLeftPop(String key) {
        return this.redisTemplate.opsForList().leftPop(key);
    }

    public Object lBLeftPop(String key, long timeout, TimeUnit unit) {
        return this.redisTemplate.opsForList().leftPop(key, timeout, unit);
    }

    public Object lRightPop(String key) {
        return this.redisTemplate.opsForList().rightPop(key);
    }

    public Object lBRightPop(String key, long timeout, TimeUnit unit) {
        return this.redisTemplate.opsForList().rightPop(key, timeout, unit);
    }

    public Object lRightPopAndLeftPush(String sourceKey, String destinationKey) {
        return this.redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
    }

    public Object lBRightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
        return this.redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey, timeout, unit);
    }

    public Long lRemove(String key, long index, String value) {
        return this.redisTemplate.opsForList().remove(key, index, value);
    }

    public void lTrim(String key, long start, long end) {
        this.redisTemplate.opsForList().trim(key, start, end);
    }

    public Long lLen(String key) {
        return this.redisTemplate.opsForList().size(key);
    }

    public Long sAdd(String key, String... values) {
        return this.redisTemplate.opsForSet().add(key, values);
    }

    public Long sRemove(String key, Object... values) {
        return this.redisTemplate.opsForSet().remove(key, values);
    }

    public Object sPop(String key) {
        return this.redisTemplate.opsForSet().pop(key);
    }

    public Boolean sMove(String key, String value, String destKey) {
        return this.redisTemplate.opsForSet().move(key, value, destKey);
    }

    public Long sSize(String key) {
        return this.redisTemplate.opsForSet().size(key);
    }

    public Boolean sIsMember(String key, Object value) {
        return this.redisTemplate.opsForSet().isMember(key, value);
    }

    public Set<Object> sIntersect(String key, String otherKey) {
        return this.redisTemplate.opsForSet().intersect(key, otherKey);
    }

    public Set<Object> sIntersect(String key, Collection<String> otherKeys) {
        return this.redisTemplate.opsForSet().intersect(key, otherKeys);
    }

    public Long sIntersectAndStore(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForSet().intersectAndStore(key, otherKey, destKey);
    }

    public Long sIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForSet().intersectAndStore(key, otherKeys, destKey);
    }

    public Set<Object> sUnion(String key, String otherKeys) {
        return this.redisTemplate.opsForSet().union(key, otherKeys);
    }

    public Set<Object> sUnion(String key, Collection<String> otherKeys) {
        return this.redisTemplate.opsForSet().union(key, otherKeys);
    }

    public Long sUnionAndStore(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
    }

    public Long sUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
    }

    public Set<Object> sDifference(String key, String otherKey) {
        return this.redisTemplate.opsForSet().difference(key, otherKey);
    }

    public Set<Object> sDifference(String key, Collection<String> otherKeys) {
        return this.redisTemplate.opsForSet().difference(key, otherKeys);
    }

    public Long sDifference(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForSet().differenceAndStore(key, otherKey, destKey);
    }

    public Long sDifference(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForSet().differenceAndStore(key, otherKeys, destKey);
    }

    public Set<Object> setMembers(String key) {
        return this.redisTemplate.opsForSet().members(key);
    }

    public Object sRandomMember(String key) {
        return this.redisTemplate.opsForSet().randomMember(key);
    }

    public List<Object> sRandomMembers(String key, long count) {
        return this.redisTemplate.opsForSet().randomMembers(key, count);
    }

    public Set<Object> sDistinctRandomMembers(String key, long count) {
        return this.redisTemplate.opsForSet().distinctRandomMembers(key, count);
    }

    public Cursor<Object> sScan(String key, ScanOptions options) {
        return this.redisTemplate.opsForSet().scan(key, options);
    }

    public Boolean zAdd(String key, String value, double score) {
        return this.redisTemplate.opsForZSet().add(key, value, score);
    }

    public Long zAdd(String key, Set<TypedTuple<Object>> values) {
        return this.redisTemplate.opsForZSet().add(key, values);
    }

    public Long zRemove(String key, Object... values) {
        return this.redisTemplate.opsForZSet().remove(key, values);
    }

    public Double zIncrementScore(String key, String value, double delta) {
        return this.redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    public Long zRank(String key, Object value) {
        return this.redisTemplate.opsForZSet().rank(key, value);
    }

    public Long zReverseRank(String key, Object value) {
        return this.redisTemplate.opsForZSet().reverseRank(key, value);
    }

    public Set<Object> zRange(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().range(key, start, end);
    }

    public Set<TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    public Set<Object> zRangeByScore(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public Set<TypedTuple<Object>> zRangeByScoreWithScores(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    public Set<TypedTuple<Object>> zRangeByScoreWithScores(String key, double min, double max, long start, long end) {
        return this.redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, start, end);
    }

    public Set<Object> zReverseRange(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    public Set<TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    public Set<Object> zReverseRangeByScore(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }

    public Set<TypedTuple<Object>> zReverseRangeByScoreWithScores(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
    }

    public Set<Object> zReverseRangeByScore(String key, double min, double max, long start, long end) {
        return this.redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, start, end);
    }

    public Long zCount(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().count(key, min, max);
    }

    public Long zSize(String key) {
        return this.redisTemplate.opsForZSet().size(key);
    }

    public Long zZCard(String key) {
        return this.redisTemplate.opsForZSet().zCard(key);
    }

    public Double zScore(String key, Object value) {
        return this.redisTemplate.opsForZSet().score(key, value);
    }

    public Long zRemoveRange(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    public Long zRemoveRangeByScore(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    public Long zUnionAndStore(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
    }

    public Long zUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
    }

    public Long zIntersectAndStore(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForZSet().intersectAndStore(key, otherKey, destKey);
    }

    public Long zIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey);
    }

    public Cursor<TypedTuple<Object>> zScan(String key, ScanOptions options) {
        return this.redisTemplate.opsForZSet().scan(key, options);
    }
}
