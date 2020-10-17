

def detect_text(path):
    """Detects text in the file."""
    from google.cloud import vision
    import io
    
    client = vision.ImageAnnotatorClient()

    with io.open(path, 'rb') as image_file:
        content = image_file.read()

    image = vision.types.Image(content=content)

    response = client.text_detection(image=image)
    contents = response.text_annotations
    # content[0]는 모든 string을 담고 있어서 배제한다
    contents = contents[1:]

    text_list = []
    for content in contents:
        text = content.description
        text = text.replace(',','')
    
        if len(text) >= 2 and has_hangul(text) == False:
            text_list.append(text)
    
    return text_list


def get_id(text_list):
    id_list = []
    for text in text_list:
        if ':' in text:
            text = text.split(':')[-1]
        if len(text) >= 2 and text.isdigit() == False:
            id_list.append(text)
    
    return id_list
        


def get_password(text_list):

    password_list = []
    for text in text_list:
        if ':' in text:
            text = text.split(':')[-1]
        if len(text) >= 8:
            removed_text = remove_special_characters(text)
            if removed_text.isalpha() == False and removed_text.isalnum() == True:
                password_list.append(text)

    return password_list


def has_hangul(text):
    import re
    
    hanCount = len(re.findall(u'[\u3130-\u318F\uAC00-\uD7A3]+', text))
    return hanCount > 0


def remove_special_characters(text):
    # special characters can be included in password
    special_characters = "~!@#$%&*?"
    for special_character in special_characters:
        text = text.replace(special_character, "")

    return text

