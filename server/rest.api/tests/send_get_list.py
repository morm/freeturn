import requests

url = 'http://159.253.20.162:8080/rest.api/InputServlet?req=get_list&'

r = requests.get("%s/" % (url))

print r.content

