import sys

from bs4 import BeautifulSoup
from selenium import webdriver

search_arg = "search?q="

nike_url = "https://www.nike.com/kr/ko_kr/" + search_arg
naver_url = 'https://www.naver.com/'

product_img_xpath = '/html/body/section/section/section/article/div[2]/div/ul/li/div/div[1]/a/span'

# Xpath_dict
product_xpath_dict = {
    'product_name_xpath': '/html/body/section/section/section/article/article[2]/div/div[4]/div/div[1]/h1/span',
    'product_detail_xpath': '/html/body/section/section/section/article/article[2]/div/div[4]/div/div[1]/div/div[1]/div/span',
    'product_price_xpath': '/html/body/section/section/section/article/article[2]/div/div[4]/div/div[1]/div/div[2]/span/strong'
    # 'product_color_xpath': '//*[@id="detail-description"]/div/div[2]/div[2]/div/dl/dd[2]',
    # 'product_size_xpath': '//*[@id="detail-description"]/div/div[2]/div[2]/div/dl/dd[3]'
}

product_result_dict = dict()