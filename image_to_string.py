import pytesseract
from PIL import Image


def image_to_string(file_name):
    text = pytesseract.image_to_string(Image.open(f"{file_name}.jpg"))
    #print(pytesseract.image_to_string(Image.open('1.jpg'), lang='eng'))

    with open(f"{file_name}.txt", "w") as text_file:
        text_file.write(text)