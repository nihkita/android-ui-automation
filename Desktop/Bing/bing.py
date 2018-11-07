from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException
import time, random

def random_line(afile):
	line = next(afile)
	for num, aline in enumerate(afile):
		if random.randrange(num + 2): continue
		line = aline
	return line

def findAll(locator, timeout=20):
	try:
		WebDriverWait(driver, timeout).until(EC.visibility_of_element_located((By.CSS_SELECTOR, locator)))
		return driver.find_elements_by_css_selector(locator)
	except TimeoutException:
		return None
	
def findOne(locator, timeout=20):
	try:
		WebDriverWait(driver, timeout).until(EC.visibility_of_element_located((By.CSS_SELECTOR, locator)))
		return driver.find_element_by_css_selector(locator)
	except TimeoutException:
		return None

driver = webdriver.Chrome()
accts = { 'derekyolands@yahoo.com'   : 'stonecold1', 
		  'francisbrown438@yahoo.com': 'dabomb10' }
        
for username in accts:
	# go to bing.com
	driver.get("http://www.bing.com/")
	
	# sign in
	findOne("#id_s").click() # click sign in
	findOne("span.id_link_text").click() # click Microsoft account connect
	findOne("#i0116").clear() # clear user name field
	findOne("#i0116").send_keys(username) # enter user name
	findOne("#i0118").clear() # clear password field
	findOne("#i0118").send_keys(accts[username]) # enter password
	findOne("#idSIButton9").click() # click sign in
    
	# search
	for i in range(random.randint(30, 40)):
		f = open("dict.txt")
		findOne("#sb_form_q").clear()
		findOne("#sb_form_q").send_keys(random_line(f))
		findOne("#sb_form_go").click()
		f.close()
		time.sleep(random.randint(10,60))
	
	# click extras
	driver.get("http://www.bing.com/rewards/dashboard")
	extras = findAll("div.offers>div:nth-child(1)>ul>li")
	for extra in extras:
		if "Tour completed" in extra.text or "Trivia challenge" in extra.text: continue
		webdriver.ActionChains(driver).key_down(Keys.CONTROL).click(extra).key_up(Keys.CONTROL).perform()

	# sign out
	time.sleep(5) # wait for user name element to stabilize
	findOne("#id_n").click() # click user name
	findOne("span.id_link_text").click() # click sign out
	
	time.sleep(random.randint(10,60))
    
driver.quit()