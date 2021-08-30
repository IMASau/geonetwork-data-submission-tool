from metcalf.imas.frontend.views import master_urls, site_content, UserSerializer
from rest_framework.views import exception_handler


def custom_exception_handler(exc, context):
    # Call REST framework's default exception handler first,
    # to get the standard error response.
    response = exception_handler(exc, context)

    # Move content into 'page' namespace
    # Now add the HTTP status code to the response.
    if response is not None:
        try:
            site = context['request'].site
        except:
            site = 1
        response.data = {
            'page': {
                'name': 'Error',
                'code': response.status_code,
                'text': response.status_text,
                'detail': response.data['detail']
            },
            'context': {
                "urls": master_urls(),
                "site": site_content(site),
                "user": UserSerializer(context['request'].user).data,
            }
        }

    return response
