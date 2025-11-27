if redis.call('exists', KEYS[1]) == 0 then
    redis.call('hset', KEYS[1], ARGV[1], ARGV[2], ARGV[3], ARGV[4], ARGV[5], ARGV[6])
    return 1
else
    return 0
end
