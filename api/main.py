'''
Assignment: Final Project Itinerary API
Author: Samuel Hudson
Course: CS496
Date: 11/26/17
'''
from google.appengine.ext import ndb
import webapp2
import json
import logging

class Itinerary(ndb.Model):
    id = ndb.StringProperty()
    owner = ndb.StringProperty()
    name = ndb.StringProperty()
    street = ndb.StringProperty()
    city = ndb.StringProperty()
    zip = ndb.StringProperty()
    duration_of_stay = ndb.StringProperty()


class ItineraryHandler(webapp2.RequestHandler):
    def post(self):
        itinerary_data = json.loads(self.request.body)
        new_itinerary = Itinerary(owner=itinerary_data['owner'],
        name=itinerary_data['name'],street=itinerary_data['street'],
        city=itinerary_data['city'],zip=itinerary_data['zip'],
        duration_of_stay=itinerary_data['duration_of_stay'])
        new_itinerary.put()
        new_itinerary.id = new_itinerary.key.urlsafe()
        new_itinerary.put()
        itinerary_dict = new_itinerary.to_dict()
        itinerary_dict['_self'] = "/itinerary/"+new_itinerary.key.urlsafe()
        self.response.write(json.dumps(itinerary_dict))
    def get(self, id=None):
        #returning itinerary entity by identifier
        if id:
            i = ndb.Key(urlsafe=id).get()
            i_d = i.to_dict()
            i_d['self'] = "/itinerary/" + id
            self.response.write(json.dumps(i.to_dict()))
        else:
            #returning all itinerary entities
            self.response.write(json.dumps([i.to_dict() 
                                            for i in Itinerary.query().fetch()]))
    def delete(self, id=None):
        if id:
            i = ndb.Key(urlsafe=id).get()
            i.key.delete()
            self.response.write("itinerary "+id+" deleted")
    def patch(self, id=None):
        if id:
            i = ndb.Key(urlsafe=id).get()
            itinerary_data = json.loads(self.request.body)
            for key in itinerary_data:
                setattr(b, key, itinerary_data[key])
            i.put()

class ItinerariesHandler(webapp2.RequestHandler):
    def get(self, sub=None):
        if sub:
            #returning all itinerary entities by owner
            self.response.write(json.dumps([i.to_dict() 
                                            for i in Itinerary.query(Itinerary.owner == sub).fetch()]))

class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.write("Final Project Itinerary API")
allowed_methods = webapp2.WSGIApplication.allowed_methods
new_allowed_methods = allowed_methods.union(('PATCH',))
webapp2.WSGIApplication.allowed_methods = new_allowed_methods
    
app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/itinerary', ItineraryHandler),
    ('/itinerary/(.*)', ItineraryHandler),
    ('/itineraries', ItinerariesHandler),
    ('/itineraries/(.*)', ItinerariesHandler),
    
], debug=True)