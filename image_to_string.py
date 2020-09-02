from pytesseract import *
from PIL import Image
import cv2
import re


def convert_color_black_and_white(img, file_name):
    BNW_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    cv2.imwrite(f"{file_name}_BNW.jpg", BNW_img)
    return BNW_img


def convert_image_to_string_BNW(file_name):
    img = cv2.imread(f"{file_name}.jpg", cv2.IMREAD_COLOR)
    BNW_img = convert_color_black_and_white(img, file_name)
    text = image_to_string(BNW_img, lang='eng')

    return text


def convert_image_to_string(file_name):
    img = cv2.imread(f"{file_name}.jpg", cv2.IMREAD_COLOR)
    text = image_to_string(img, lang='eng')

    return text


def save_text_file(file_name, text):
    with open(f"{file_name}.txt", "w") as text_file:
        text_file.write(text)


def get_password(text):
    password = []
    splited_texts = re.split(' |\n', text)
    for splited_text in splited_texts:
        if len(splited_text) >= 8 and splited_text.isalnum() is True:
            password.append(splited_text)

    return password


def is_answer(file_name, text):
    with open(f"answer/{file_name}.txt", "r") as text_file:
        answer_text = text_file.readline()
    if(answer_text == text):
        return True
    else:
        return False
#print(get_password("dddddddddd ddd "))
