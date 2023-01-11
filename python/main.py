import os
import cgi
import webapp2
import httplib2
import logging

from apiclient.discovery import build
from oauth2client.appengine import oauth2decorator_from_clientsecrets
from oauth2client.client import AccessTokenRefreshError

from google.appengine.api import users
from google.appengine.api import memcache
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

class User(db.Model):
	identity = db.UserProperty()
	name     = db.StringProperty()
	pic_url  = db.StringProperty()
	location = db.GeoPtProperty()
	status   = db.StringProperty()
	links    = db.StringListProperty()

class Message(db.Model):
	sender    = db.ReferenceProperty(User, required=True, collection_name='message_sender')
	reciever  = db.ReferenceProperty(User, required=True, collection_name='message_reciever')
	text      = db.StringProperty()
	date_time = db.DateTimeProperty(auto_now=True)
	
# CLIENT_SECRETS, name of a file containing the OAuth 2.0 information for this
# application, including client_id and client_secret, which may be downloaded
# from API Access tab on the Google APIs Console <http://code.google.com/apis/console>
CLIENT_SECRETS = os.path.join(os.path.dirname(__file__), 'client_secrets.json')

# Helpful message to display in the browser if the CLIENT_SECRETS file
# is missing.
MISSING_CLIENT_SECRETS_MESSAGE = """
<h1>Warning: Please configure OAuth 2.0</h1><p>
To make this sample run you will need to populate the client_secrets.json file
found at:</p><code>%s</code>.<p>with information found on the
<a href="https://code.google.com/apis/console">APIs Console</a>.</p>
""" % CLIENT_SECRETS

http = httplib2.Http(memcache)
service = build("plus", "v1", http=http)
decorator = oauth2decorator_from_clientsecrets(
    CLIENT_SECRETS,
    scope='https://www.googleapis.com/auth/plus.me',
    message=MISSING_CLIENT_SECRETS_MESSAGE)


from oauth2client.client import flow_from_clientsecrets

class IndexHandler(webapp2.RequestHandler):

	def get(self):
		if users.get_current_user():
			logging.info(repr(users.get_current_user()))
			self.response.out.write(users.create_logout_url('/', '/'))
			#self.redirect("/app")
		else:
			flow = flow_from_clientsecrets(CLIENT_SECRETS,
											scope='https://www.googleapis.com/auth/plus.me',
											redirect_uri='http://localhost:8080/oauth2callback')
			auth_uri = flow.step1_get_authorize_url()	
		
			path = os.path.join(os.path.dirname(__file__), 'templates/login.html')
			variables = {
				'url' : auth_uri
			}
			self.response.out.write(template.render(path, variables))
	
class CallbackHandler(webapp2.RequestHandler):
	def get(self):
		flow = flow_from_clientsecrets(CLIENT_SECRETS,
											scope='https://www.googleapis.com/auth/plus.me',
											redirect_uri='http://localhost:8080/oauth2callback')
		credentials = flow.step2_exchange(self.request.get('code'))
		self.response.out.write(self.request.get('code'))
	
class LoginHandler(webapp2.RequestHandler):

	@decorator.oauth_aware
	def get(self):
		try:
			http = decorator.http()
			mePerson = service.people().get(userId='me').execute(http=http)
			
			curr_user = users.get_current_user()
			user = User.gql("WHERE identity = :1", curr_user).get()
			
			if user is None:
				user = User()
				user.identity = curr_user
				user.name     = '%s' % mePerson['displayName']
				user.pic_url  = '%s' % mePerson['image']['url']
				user.links    = []
				user.put()
			
			variables = {
				'user_name':       user.identity.nickname(),
				'user_image_url' : user.pic_url,
				'url':             users.create_logout_url('/', '/')
			}
			path = os.path.join(os.path.dirname(__file__), 'templates/index.html')
			self.response.out.write(template.render(path, variables))
		except:  # AccessTokenRefreshError:
			path = os.path.join(os.path.dirname(__file__), 'templates/login.html')
			variables = {
				'url': decorator.authorize_url()
			}
			self.response.out.write(template.render(path, variables))

class MessageHandler(webapp2.RequestHandler):

	def get(self):
		self.response.out.write('{"text": "Ho ho!"}')
		
	def post(self):
		self.response.out.write('{"status": "ok"}')
		
class ProfileHandler(webapp2.RequestHandler):

	def get(self):
		self.response.out.write('{"text": "User #1"}')
		
	def post(self):
		self.response.out.write('{"status": "ok"}')

#class ServiceHandler(webapp2.RequestHandler):
#
#    @decorator.oauth_aware
#    def post(self):
#        path = os.path.join(os.path.dirname(__file__), 'templates/response.html')
#
#        http = decorator.http()
#        mePerson = service.people().get(userId='me').execute(http=http)
#        logging.info(repr(mePerson))
#        user_name = '%s' % mePerson['displayName']
#        user_image_url = '%s' % mePerson['image']['url'];
#        variables = {
#               'user_name': user_name ,
#               'user_image_url' : user_image_url,
#               'url': users.create_logout_url('/', '/'),
#               'has_credentials': decorator.has_credentials(),
#               'message' : cgi.escape(self.request.get('content')),
#               'user_agent' : self.request.headers['User-Agent'],
#               'version' : os.environ['SERVER_SOFTWARE']
#        }
#        self.response.out.write(template.render(path, variables))


#class RedirectHomeHandler(webapp2.RequestHandler):
#
#    def post(self):
#        self.redirect("/app")


app = webapp2.WSGIApplication([('/', IndexHandler),
                               ('/oauth2callback', CallbackHandler),
                               #('/', LoginHandler),
                               ('/message', MessageHandler),
                               ('/profile', ProfileHandler),
                               #('/response', ServiceHandler),
                               #('/home', RedirectHomeHandler),
                               (decorator.callback_path, decorator.callback_handler())],
                                debug=True)
