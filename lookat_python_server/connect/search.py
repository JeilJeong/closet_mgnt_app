import sys
import shutil
import subprocess
import chromedriver_autoinstaller
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium import webdriver
from chrome_info import *
from scraping_constant import *
from conn_constant import inputData

def get_product_info() :
    product_exist_flag = True

    # user_agent = get_useragent()

    # this method prevented by nike online page
    # # ---------------------------implement part-----------------------------
    # options = webdriver.ChromeOptions()
    # options.headless = False
    # # background에서 돌아가는 chrome에서 보이지 않지만 얼만큼의 size로 띄울 것인지 options.add_argument()로 결정
    # options.add_argument("window-size=1920x1080")
    # # for headless chrome user-agent
    # options.add_argument("user-agent=" + user_agent)

    # browser = webdriver.Chrome(options=options)
    # browser.maximize_window()

    # browser.get(nike_url)

    # this method is alternative way to access information
    try:
        # remove cookie/cache file before load the browser
        shutil.rmtree(r"c:\\chrometemp")
    except FileNotFoundError:
        pass

    # --------------------------- open browser by local chrome intallment root ------------------------------
    proc = subprocess.Popen(
        r'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\\chrometemp"')  # 디버거 크롬 구동
    # --------------------------------------- insert chrome option ------------------------------------------
    option = Options()
    option.add_experimental_option("debuggerAddress", "127.0.0.1:9222")

    chrome_ver = chromedriver_autoinstaller.get_chrome_version().split('.')[0]
    try:
        browser = webdriver.Chrome(
            f'./{chrome_ver}/chromedriver.exe', options=option)
    except:
        chromedriver_autoinstaller.install(True)
        browser = webdriver.Chrome(
            f'./{chrome_ver}/chromedriver.exe', options=option)

    # --------------------------- open browser by local chrome intallment root ------------------------------
    browser.implicitly_wait(10)  # timeout flag
    browser.get(nike_url)

    try:
        product_view = browser.find_element_by_xpath(product_img_xpath)
    except Exception as e:
        product_exist_flag = False

    if (product_exist_flag == False):
        browser.quit()
        # 결과에 대해 보내주는 추가 작업 필요
    else:
        product_view.click()
        for key in product_xpath_dict:
            WebDriverWait(browser, 15).until(EC.presence_of_element_located(
                (By.XPATH, product_xpath_dict[key])))
            product_result_dict[key] = browser.find_element_by_xpath(
                product_xpath_dict[key]).get_attribute('innerHTML')
        for key in product_result_dict:
            print(key, end=' : ')
            print(product_result_dict[key])
        inputData = product_result_dict
    proc.kill()
