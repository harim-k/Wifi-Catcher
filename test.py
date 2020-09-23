strr = '가1'

print(strr.isalnum())


def has_hangul(text):
    import re
    
    hanCount = len(re.findall(u'[\u3130-\u318F\uAC00-\uD7A3]+', text))
    return hanCount > 0


print(has_hangul(strr))