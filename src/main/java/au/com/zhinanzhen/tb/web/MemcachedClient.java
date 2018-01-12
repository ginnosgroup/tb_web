package au.com.zhinanzhen.tb.web;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikasoa.core.utils.StringUtil;

import net.rubyeye.xmemcached.XMemcachedClient;

/**
 * Memcached操作类
 * 
 * @author <a href="mailto:leisu@zhinanzhen.org">sulei</a>
 * @version 0.1
 */
public class MemcachedClient {
	private static final Logger LOG = LoggerFactory.getLogger(MemcachedClient.class);
	@Resource
	private XMemcachedClient xMemcachedClient;

	private final static String CACHE_PREFIX = "OH_WEB_";

	private final static int CACHE_FAIL_EXPIRATION = 7000;

	public boolean set(String key, Object value) throws Exception {
		if (xMemcachedClient == null) {
			LOG.warn("memcached is null !");
			return false;
		}
		if (StringUtil.isEmpty(key) || value == null) {
			LOG.warn("memcached key or value is null !");
			return false;
		}
		return xMemcachedClient.set(CACHE_PREFIX + key, CACHE_FAIL_EXPIRATION, value);
	}

	public Object get(String key) throws Exception {
		if (xMemcachedClient == null) {
			LOG.warn("memcached is null !");
			return null;
		}
		if (StringUtil.isEmpty(key)) {
			LOG.warn("memcached key is null !");
			return null;
		}
		return xMemcachedClient.get(CACHE_PREFIX + key);
	}

	public boolean del(String key) throws Exception {
		if (xMemcachedClient == null) {
			LOG.warn("memcached is null !");
			return false;
		}
		if (StringUtil.isEmpty(key)) {
			LOG.warn("memcached key is null !");
			return false;
		}
		return xMemcachedClient.delete(CACHE_PREFIX + key);
	}
}