package id.latenight.creativepos.util;

public class URI {

    private static String url_parent = "https://aplikasi.cizypos.com/";

    public static String URL()
    {
        return "https://sembakobg.cizypos.com/authentication/";
    }

    public static String GET_APK_VERSION()
    {
        return url_parent+"api/get_apk_version";
    }

    public static String GET_APK_DOWNLOAD()
    {
        return url_parent+"aplikasi/get_apk_download";
    }

    public static String UPDATE_SET_APK()
    {
        return url_parent+"api/update_set_apk/";
    }

    public static String PUSH_APK_NOTIFICATION()
    {
        return url_parent+"api/push_notification/";
    }

    public static String API_GET_UNITS(String url) {
        return "http://"+url+"/api/units/";
    }
    public static String API_GET_TOTAL_SALES_TODAY(String url) {
        return "http://"+url+"/api/get_total_sale";
    }

    public static String API_GET_PRODUCT_SALES_TODAY(String url) {
        return "http://"+url+"/api/get_total_product";
    }

    public static String API_LOGIN(String url) {
        return "http://"+url+"/api/login/";
    }
    public static String API_RESET_PASSWORD(String url) {
        return "http://"+url+"/api/reset_password/";
    }
    public static String CHECK_OPEN_REGISTER(String url) {
        return "http://"+url+"/api/checkOpenRegistration/";
    }
    public static String BALANCE_OPEN_REGISTER(String url) {
        return "http://"+url+"/api/addBalance/";
    }
    public static String BALANCE_CLOSE_REGISTER(String url) {
        return "http://"+url+"/api/closeRegister/";
    }
    public static String API_MENU(String url) {
        return "http://"+url+"/api/menu/";
    }
    public static String API_CUSTOMER(String url) {
        return "http://"+url+"/api/customer/";
    }
    public static String API_ADD_CUSTOMER(String url) {
        return "http://"+url+"/api/add_customer/";
    }
    public static String API_TABLE(String url){
        return "http://"+url+"/api/table/";
    }
    public static String API_PLACE_ORDER(String url) {
        return "http://"+url+"/api/place_order_with_shipping/";
    }
    public static String API_HOLD_ORDER(String url){
        return "http://"+url+"/api/hold_order/";
    }
    public static String API_INVOICE(String url){
        return "http://"+url+"/api/invoice/";
    }
    public static String API_DAILY_REPORT_LIST(String url) {
        return "http://"+url+"/api/daily_report_user_list/";
    }
    public static String API_DAILY_REPORT(String url) {
        return "http://"+url+"/api/daily_report_user/";
    }
    public static String API_DAILY_PRODUCT_REPORT_LIST(String url) {
        return "http://"+url+"/api/daily_product_report_list/";
    }
    public static String API_DAILY_PRODUCT_REPORT(String url) {
        return "http://"+url+"/api/daily_product_report/";
    }
    public static String API_FINISH_ORDER(String url) {
        return "http://"+url+"/api/update_order_status/";
    }
    public static String API_NEW_ORDER(String url){
        return "http://"+url+"/api/new_orders_user";
    }
    public static String API_ALL_ORDER(String url){
        return "http://"+url+"/api/all_orders";
    }
    public static String API_TEN_SALES(String url){
        return "http://"+url+"/api/ten_sales_user";
    }
    public static String API_PAYMENT_METHOD(String url) {
        return "http://"+url+"/api/payment_methods";
    }
    public static String API_LOGISTIC(String url){
        return "http://"+url+"/api/logistics/";
    }
    public static String API_MAIN_CATEGORIES(String url) {
        return "http://"+url+"/api/main_categories_mobile/";
    }
    public static String API_CATEGORIES(String url) {
        return "http://"+url+"/api/categories_mobile/";
    }
    public static String API_LABELS(String url){
        return "http://"+url+"/api/labels_mobile/";
    }
    public static String API_PPN(String url){
        return "http://"+url+"/api/ppn";
    }
    public static String API_DETAIL_ORDER(String url){
        return "http://"+url+"/api/all_information_of_a_sale/";
    }
    public static String API_DELETE_SALES(String url){
        return "http://"+url+"/api/delete_sale/";
    }
    public static String PATH_IMAGE(String url){
        return "http://"+url+"/assets/POS/images/";
    }
    public static String API_LOGO(String url){
        return "http://"+url+"/api/logo/";
    }
    public static String API_STOCK_OUT(String url) {
        return "http://"+url+"/api/total_stock_keluar/";
    }
    public static String API_IMAGE_BILL(String url) {
        return "http://"+url+"/assets/images/logo_print.png";
    }
    public static String API_EXPENSES(String url){
        return "http://"+url+"/api/expenses/";
    }
    public static String API_CREATE_EXPENSE(String url) {
        return "http://"+url+"/api/add_expense/";
    }
    public static String API_DELETE_EXPENSE(String url) {
        return "http://"+url+"/api/delete_expense/";
    }
    public static String API_EXPENSE_CATEGORIES(String url) {
        return "http://"+url+"/api/expense_categories/";
    }
    public static String API_SUMMARY_REPORT(String url) {
        return "http://"+url+"/api/summary_report/";
    }
    public static String API_SUMMARY_REPORT_DAILY(String url) {
        return "http://"+url+"/api/summary_report_daily/";
    }
    public static String API_INGREDIENTS_LIST(String url) {
        return "http://"+url+"/api/ingredients_list/";
    }

    public static String API_INGREDIENTS_CONVERT(String url, String name) {
        return "http://"+url+"/api/ingredient_convert/"+name;
    }
    public static String API_INGREDIENTS_BY_OUTLET(String url) {
        return "http://"+url+"/api/ingredients_by_outlet/";
    }
    public static String API_RECEIVED_STOCK(String url) {
        return "http://"+url+"/api/received_stock/";
    }
    public static String API_CONVERT_INGREDIENTS(String url) {
        return "http://"+url+"/api/convert_ingredient/";
    }
    public static String API_INVENTORY_RECEIVE_LIST(String url){
        return "http://"+url+"/api/inventory_receive_list/";
    }
    public static String API_SUPPLIERS_LIST(String url) {
        return "http://"+url+"/api/suppliers_list/";
    }
    public static String API_OUTLETS_LIST(String url) {
        return "http://"+url+"/api/outlets_list/";
    }
    public static String API_PURCHASE_ORDER(String url) {
        return "http://"+url+"/api/purchase_order/";
    }
    public static String API_CREATE_PRODUCTION(String url) {
        return "http://"+url+"/api/create_production/";
    }
    public static String API_PRODUCTION_LIST(String url){
        return "http://"+url+"/api/production_list/";
    }
    public static String API_PRODUCTION_AGAIN(String url) {
        return "http://"+url+"/api/production_again/";
    }
    public static String API_PURCHASE_LIST(String url){
        return "http://"+url+"/api/purchase_list/";
    }
    public static String API_PURCHASE_DETAIL(String url){
        return "http://"+url+"/api/purchase_detail/";
    }
    public static String API_DELETE_PURCHASE(String url) {
        return "http://"+url+"/api/delete_purchase/";
    }
    public static String API_INCOME_STATEMENT(String url) {
        return "http://"+url+"/api/income_statement/";
    }
    public static String API_SUMMARY_REPORT_ALL_OUTLET(String url) {
        return "http://"+url+"/api/summary_report_all_outlet/";
    }
    public static String API_SUMMARY_REPORT_GRAPHIC(String url) {
        return "http://"+url+"/api/summary_report_graphic/";
    }
    public static String API_PRODUCTS_LIST(String url) {
        return "http://"+url+"/api/products/";
    }
    public static String API_CREATE_PRODUCT(String url) {
        return "http://"+url+"/api/create_product/";
    }
    public static String API_UPDATE_PRODUCT(String url) {
        return "http://"+url+"/api/update_product/";
    }
    public static String API_DETAIL_PRODUCT(String url){
        return "http://"+url+"/api/detail_product/";
    }
    public static String API_DELETE_PRODUCT(String url){
        return "http://"+url+"/api/delete_product/";
    }

    public static String API_INGREDIENT_LIST(String url) {
        return "http://"+url+"/api/ingredients/";
    }
    public static String API_GROUP_OUTLETS_LIST(String url) {
        return "http://"+url+"/api/group_outlets_list/";
    }
    public static String API_UNIT_INGREDIENT(String url) {
        return "http://"+url+"/api/unit_ingredient/";
    }
    public static String API_CATEGORY_INGREDIENT(String url) {
        return "http://"+url+"/api/category_ingredient/";
    }
    public static String API_CREATE_INVENTORY(String url) {
        return "http://"+url+"/api/create_ingredient/";
    }
    public static String API_UPDATE_INVENTORY(String url) {
        return "http://"+url+"/api/update_ingredient/";
    }
    public static String API_DETAIL_INVENTORY(String url){
        return "http://"+url+"/api/detail_ingredient/";
    }

    public static String API_DELETE_INVENTORY(String url)
    {
        return "http://"+url+"/api/delete_ingredient/";
    }

    public static String API_INVENTORY_RECEIVE_DETAIL(String url){
        return "http://"+url+"/api/inventory_receive_detail/";
    }
    public static String API_PRODUCTION_DETAIL(String url){
        return "http://"+url+"/api/production_detail/";
    }
    public static String API_INVENTORY_TRANSFER(String url) {
        return "http://"+url+"/api/transfer_stock/";
    }
    public static String API_INVENTORY_TRANSFER_DETAIL(String url) {
        return "http://"+url+"/api/inventory_transfer_detail/";
    }
    public static String API_PROCESS_INVENTORY_TRANSFER(String url) {
        return "http://"+url+"/api/process_inventory_transfer/";
    }

//    public static String API_LOGIN = "http://retail.cizypos.com/api/login/";
//    public static String CHECK_OPEN_REGISsummary_report_dailyTER = "http://retail.cizypos.com/api/checkOpenRegistration/";
//    public static String BALANCE_OPEN_REGISTER = "http://retail.cizypos.com/api/addBalance/";
//    public static String BALANCE_CLOSE_REGISTER = "http://retail.cizypos.com/api/closeRegister/";
//    public static String API_MENU = "http://retail.cizypos.com/api/menu/";
//    public static String API_CUSTOMER = "http://retail.cizypos.com/api/customer/";
//    public static String API_TABLE = "http://retail.cizypos.com/api/table/";
//    public static String API_PLACE_ORDER = "http://retail.cizypos.com/api/place_order_with_shipping/";
//    public static String API_HOLD_ORDER = "http://retail.cizypos.com/api/hold_order/";
//    public static String API_INVOICE = "http://retail.cizypos.com/api/invoice/";
//    public static String API_DAILY_REPORT = "http://retail.cizypos.com/api/daily_report_user/";
//    public static String API_FINISH_ORDER = "http://retail.cizypos.com/api/update_order_status/";
//    public static String API_NEW_ORDER = "http://retail.cizypos.com/api/new_orders_user";
//    public static String API_ALL_ORDER = "http://retail.cizypos.com/api/ten_sales";
//    public static String API_TEN_SALES = "http://retail.cizypos.com/api/ten_sales_user";
//    public static String API_PAYMENT_METHOD = "http://retail.cizypos.com/api/payment_methods";
//    public static String API_MAIN_CATEGORIES = "http://retail.cizypos.com/api/main_categories/";
//    public static String API_CATEGORIES = "http://retail.cizypos.com/api/categories/";
//    public static String API_LABELS = "http://retail.cizypos.com/api/labels/";
//    public static String API_LOGISTIC = "http://retail.cizypos.com/api/logistics/";
//    public static String API_PPN = "http://retail.cizypos.com/api/ppn/";
//    public static String API_DETAIL_ORDER = "http://retail.cizypos.com/api/all_information_of_a_sale/";
//    public static String API_DELETE_SALES = "http://retail.cizypos.com/api/delete_sale/";
//    public static String PATH_IMAGE = "http://retail.cizypos.com/assets/POS/images/";
//    public static String API_ADD_CUSTOMER = "http://retail.cizypos.com/api/add_customer/";
}