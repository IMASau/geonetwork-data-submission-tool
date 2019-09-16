import subprocess
from django import template

register = template.Library()


@register.simple_tag()
def gitrev():
    try:
        p = subprocess.Popen(["git", "show", "HEAD", "--no-patch", "--no-notes", "--date=short", '--pretty=%h %cd'],
                             stdout=subprocess.PIPE)
        gv = p.communicate()[0]
        return gv.strip().decode('UTF-8')

    except:
        return '(unknown)'
