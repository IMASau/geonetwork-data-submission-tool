
This is a Django based web application.

Typical install steps would be:

```sh
cd MetadataServer
virtualenv venv
source venv/bin/activate
pip install -r requirements.txt
python manage.py syncdb
python manage.py runserver
```

Additional steps when setting up locally:

### Load external sources (IMAS)
```
python3 manage.py load_parameterunits
python3 manage.py load_parameterplatforms
python3 manage.py load_parameternames
python3 manage.py load_parameterinstruments
python3 manage.py load_institutions
python3 manage.py load_persons
python3 manage.py load_rolecodes
python3 manage.py load_samplingfrequencies
python3 manage.py load_topiccategories
python3 manage.py load_sciencekeywords
python3 manage.py load_horizontalresolutions
```

---

For production you'll want to add a `webapp/local_settings.py` and configure a 
different database, static file hosting details etc.

