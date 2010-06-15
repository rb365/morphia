/**
 * Copyright (C) 2010 Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.morphia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.EntityListeners;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.PostLoad;
import com.google.code.morphia.annotations.PostPersist;
import com.google.code.morphia.annotations.PreLoad;
import com.google.code.morphia.annotations.PrePersist;
import com.google.code.morphia.annotations.Transient;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.testmodel.Address;
import com.google.code.morphia.testmodel.Hotel;
import com.google.code.morphia.testmodel.Rectangle;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 *
 * @author Scott Hernandez
 */
public class TestDatastore  extends TestBase{

	@Entity("facebook_users")
	public static class FacebookUser {
		@Id long id;
		String username;
		public FacebookUser() {}
		public FacebookUser(long id, String name) {
			this(); this.id = id; this.username = name;
		}
	}
	
	public static class LifecycleListener {
		static boolean prePersist = false;
		static boolean prePersistWithEntity = false;
		
		@PrePersist
		void PrePersist() {
			prePersist = true;
		}
		
		@PrePersist
		void PrePersist(LifecycleTestObj obj) {
			if(obj == null) throw new RuntimeException();
			prePersistWithEntity = true;
			
		}
	}
	
	@EntityListeners(LifecycleListener.class)
	public static class LifecycleTestObj {
		@Id String id;
		@Transient boolean prePersist, postPersist, preLoad, postLoad, postLoadWithParam;
		boolean prePersistWithParamAndReturn, prePersistWithParam;
		boolean postPersistWithParam;
		boolean preLoadWithParamAndReturn, preLoadWithParam;
		
		@PrePersist
		void PrePersist() {
			prePersist = true;
		}
		
		@PrePersist
		protected void PrePersistWithParam(DBObject dbObj) {
			prePersistWithParam = true;
		}
		
		@PrePersist
		public DBObject PrePersistWithParamAndReturn(DBObject dbObj) {
			prePersistWithParamAndReturn = true;
			return null;
//			DBObject retObj = new BasicDBObject((Map)dbObj);
//			retObj.put("prePersistWithParamAndReturn", true);
//			return retObj;
		}
		
		@SuppressWarnings("unused")
		@PostPersist
		private void PostPersistPersist() {
			postPersist = true;
		}
		
		@PostPersist
		void PostPersistWithParam(DBObject dbObj) {
//			dbObj.put("postPersistWithParam", true);
			postPersistWithParam = true;
			if(!dbObj.containsField(Mapper.ID_KEY)) throw new RuntimeException("missing " + Mapper.ID_KEY);
		}

		@PreLoad
		void PreLoad() {
			preLoad = true;
		}
		
		@PreLoad
		void PreLoadWithParam(DBObject dbObj) {
			dbObj.put("preLoadWithParam", true);
		}
		
		@SuppressWarnings("unchecked")
		@PreLoad
		DBObject PreLoadWithParamAndReturn(DBObject dbObj) {
			DBObject retObj = new BasicDBObject((Map)dbObj);
			retObj.put("preLoadWithParamAndReturn", true);
			return retObj;
		}

		@PostLoad
		void PostLoad() {
			postLoad = true;
		}
		
		@PreLoad
		void PostLoadWithParam(DBObject dbObj) {
			postLoadWithParam = true;
//			dbObj.put("postLoadWithParam", true);
		}
	}
	
	public static class KeysKeysKeys {
		@Id String id;
		List<Key<FacebookUser>> users;
		Key<Rectangle> rect;
		
		protected KeysKeysKeys() {}
		public KeysKeysKeys(Key<Rectangle> rectKey, List<Key<FacebookUser>> users) {
			this.rect = rectKey;
			this.users = users;
		}
	}
	
	public TestDatastore () {
		try {
			mongo = new Mongo();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		morphia.map(Hotel.class).map(KeysKeysKeys.class).map(Rectangle.class).map(FacebookUser.class);
		//delete, and (re)create test db
	}

	@Test
    public void testLowlevelbyteArray() throws Exception {
	    Mongo m = new Mongo();
		DBCollection c = m.getDB("test").getCollection( "testBinary" );
	    c.drop();
	    DBObject loaded;
	    Iterator<DBObject> it = c.find(new BasicDBObject(), null, 0, 1);
	    if (it != null && it.hasNext()) loaded = it.next();
	    
	    c.save( BasicDBObjectBuilder.start().add( "a" , "eliot".getBytes() ).get() );
	    
	    DBObject out = c.findOne();
	    loaded = c.find(new BasicDBObject(), null, 0, 1).next();
	    assertEquals(new String((byte[])out.get("a")), new String((byte[])loaded.get("a")));
	    byte[] b = (byte[])(out.get( "a" ) );
	    assertEquals( "eliot" , new String( b ) );
	}
	
	@Test
    public void testLifecycle() throws Exception {
		LifecycleTestObj life1 = new LifecycleTestObj();
		((DatastoreImpl)ds).getMapper().addMappedClass(LifecycleTestObj.class);
		ds.save(life1);
		assertTrue(life1.prePersist);
		assertTrue(life1.prePersistWithParam);
		assertTrue(life1.prePersistWithParamAndReturn);
		assertTrue(life1.postPersist);
		assertTrue(life1.postPersistWithParam);
		
		LifecycleTestObj loaded = ds.get(life1);
		assertTrue(loaded.preLoad);
		assertTrue(loaded.preLoadWithParam);
		assertTrue(loaded.preLoadWithParamAndReturn);
		assertTrue(loaded.postLoad);
		assertTrue(loaded.postLoadWithParam);
	}
	
	@Test
    public void testLifecycleListeners() throws Exception {
		LifecycleTestObj life1 = new LifecycleTestObj();
		((DatastoreImpl)ds).getMapper().addMappedClass(LifecycleTestObj.class);
		ds.save(life1);
		assertTrue(LifecycleListener.prePersist);
		assertTrue(LifecycleListener.prePersistWithEntity);
	}
	@Test
    public void testCollectionNames() throws Exception {
		assertEquals("facebook_users", morphia.getMapper().getCollectionName(FacebookUser.class));
	}
	@Test
    public void testGet() throws Exception {
		List<FacebookUser> fbUsers = new ArrayList<FacebookUser>();
		fbUsers.add(new FacebookUser(1,"user 1"));
		fbUsers.add(new FacebookUser(2,"user 2"));
		fbUsers.add(new FacebookUser(3,"user 3"));
		fbUsers.add(new FacebookUser(4,"user 4"));
		
	
		ds.save(fbUsers);
		assertEquals(4, ds.getCount(FacebookUser.class));
		assertNotNull(ds.get(FacebookUser.class, 1));
		List<Long> ids = new ArrayList<Long>(2);
		ids.add(1L); ids.add(2L);
		List<FacebookUser> res = ds.get(FacebookUser.class, ids).asList();
		assertEquals(res.size(), 2);
		assertNotNull(res.get(0));
		assertNotNull(res.get(0).id);
		assertNotNull(res.get(1));
		assertNotNull(res.get(1).username);
	}
	public void testIdUpdatedOnSave() throws Exception {
		Rectangle rect = new Rectangle(10, 10);
		ds.save(rect);
		assertNotNull(rect.getId());
	}

	@Test
    public void testSaveAndDelete() throws Exception {
		Rectangle rect = new Rectangle(10, 10);
		rect.setId("1");
		
		
		//test delete(entity)
		ds.save(rect);
		assertEquals(1, ds.getCount(rect));
		ds.delete(rect);
		assertEquals(0, ds.getCount(rect));

		//test delete(entity, id)
		ds.save(rect);
		assertEquals(1, ds.getCount(rect));
		ds.delete(rect.getClass(), 1);
		assertEquals(1, ds.getCount(rect));
		ds.delete(rect.getClass(), "1");
		assertEquals(0, ds.getCount(rect));

		//test delete(entity, {id})
		ds.save(rect);
		assertEquals(1, ds.getCount(rect));
		List<String> ids = new ArrayList<String>();
		ids.add("1");
		ds.delete(rect.getClass(), ids);
		assertEquals(0, ds.getCount(rect));

		//test delete(entity, {id,id})
		rect.setId("1");
		ds.save(rect);
		rect.setId("2");
		ds.save(rect);
		assertEquals(2, ds.getCount(rect));
		ids.clear(); ids.add("1"); ids.add("2");
		ds.delete(rect.getClass(), ids);
		assertEquals(0, ds.getCount(rect));
		
		//test delete(entity, {id,id}) with autogenerated ids
		ids.clear();
		rect.setId(null); // rect1
		ds.save(rect);
		ids.add(rect.getId());
		rect.setId(null); // rect2
		ds.save(rect);
		ids.add(rect.getId());
		assertEquals("datastore should have saved two entities with autogenerated ids", 2, ds.getCount(rect));
		ds.delete(rect.getClass(), ids);
		assertEquals("datastore should have deleted two entities with autogenerated ids", 0, ds.getCount(rect));

		//test delete(entity, {id}) with one left
		rect.setId("1");
		ds.save(rect);
		rect.setId("2");
		ds.save(rect);
		assertEquals(2, ds.getCount(rect));
		ids.clear(); ids.add("1");
		ds.delete(rect.getClass(), ids);
		assertEquals(1, ds.getCount(rect));

		//test delete(Class, {id}) with one left
		rect.setId("1");
		ds.save(rect);
		rect.setId("2");
		ds.save(rect);
		assertEquals(2, ds.getCount(rect));
		ids.clear(); ids.add("1");
		ds.delete(Rectangle.class, ids);
		assertEquals(1, ds.getCount(rect));
	}
	
    @Test
    public void testEmbedded() throws Exception {
        Hotel borg = Hotel.create();
        borg.setName("Hotel Borg");
        borg.setStars(4);
        borg.setTakesCreditCards(true);
        borg.setStartDate(new Date());
        borg.setType(Hotel.Type.LEISURE);
        Address borgAddr = new Address();
        borgAddr.setStreet("Posthusstraeti 11");
        borgAddr.setPostCode("101");
        borg.setAddress(borgAddr);

        
        ds.save(borg);
        assertEquals(1, ds.getCount(Hotel.class));
        assertNotNull(borg.getId());

        Hotel hotelLoaded = ds.get(Hotel.class, borg.getId());
        assertEquals(borg.getName(), hotelLoaded.getName());
        assertEquals(borg.getAddress().getPostCode(), hotelLoaded.getAddress().getPostCode());
        
    }
    
}
