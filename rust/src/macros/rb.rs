#[macro_export]
macro_rules! impl_count_by_condition {
    ($table:ty) => {
        $crate::impl_count_by_condition!($table, "");
    };
    ($table:ty, $table_name:expr) => {
        impl $table {
            pub async fn count_by_condition(
                executor: &dyn rbatis::executor::Executor,
                condition: rbs::Value
            ) -> std::result::Result<u64, rbatis::rbdc::Error> {
                use rbatis::crud_traits::ValueOperatorSql;

                #[rbatis::py_sql(
                    "`select count(*) from ${table_name}`  
                     trim end=' where ':  
                       ` where `  
                       trim ' and ': for key,item in condition:  
                                    if item == null:  
                                       continue:  
                                    if !item.is_array():  
                                      ` and ${key.operator_sql()}#{item}`  
                                    if item.is_array():  
                                      ` and ${key} in (`  
                                         trim ',': for _,item_array in item:  
                                              #{item_array},  
                                      `)`"
                )]
                async fn count_by_condition_impl(
                    executor: &dyn rbatis::executor::Executor,
                    table_name: String,
                    condition: &rbs::Value
                ) -> std::result::Result<u64, rbatis::rbdc::Error> {
                    rbatis::impled!()
                }

                let mut table_name = $table_name.to_string();
                if table_name.is_empty() {
                    #[rbatis::snake_name($table)]
                    fn snake_name() {}
                    table_name = snake_name();
                }
                count_by_condition_impl(executor, table_name, &condition).await
            }
        }
    };
}

#[macro_export]
macro_rules! impl_select_one_by_condition {
    ($table:ty) => {
         $crate::impl_select_one_by_condition!($table, "");
    };
    ($table:ty, $table_name:expr) => {
        impl $table {
            pub async fn select_one_by_condition(
                executor: &dyn rbatis::executor::Executor,
                condition: rbs::Value
            ) -> std::result::Result<Option<$table>, rbatis::rbdc::Error> {
                use rbatis::crud_traits::ValueOperatorSql;

                #[rbatis::py_sql(
                    "`select * from ${table_name}`  
                     trim end=' where ':  
                       ` where `  
                       trim ' and ': for key,item in condition:  
                                    if item == null:  
                                       continue:  
                                    if !item.is_array():  
                                      ` and ${key.operator_sql()}#{item}`  
                                    if item.is_array():  
                                      ` and ${key} in (`  
                                         trim ',': for _,item_array in item:  
                                              #{item_array},  
                                      `)`  
                     ` limit 1`"
                )]
                async fn select_one_by_condition_impl(
                    executor: &dyn rbatis::executor::Executor,
                    table_name: String,
                    condition: &rbs::Value
                ) -> std::result::Result<Option<$table>, rbatis::rbdc::Error> {
                    rbatis::impled!()
                }

                let mut table_name = $table_name.to_string();
                if table_name.is_empty() {
                    #[rbatis::snake_name($table)]
                    fn snake_name() {}
                    table_name = snake_name();
                }
                select_one_by_condition_impl(executor, table_name, &condition).await
            }
        }
    };
}

#[macro_export]
macro_rules! impl_select_by_id {
    ($table:ty) => {
        $crate::impl_select_by_id!($table, "");
    };
    ($table:ty, $table_name:expr) => {
        #[allow(non_local_definitions)]
        impl $table {
            pub async fn select_by_id<T>(
                executor: &dyn rbatis::executor::Executor,
                id: T
            ) -> std::result::Result<Option<$table>, rbatis::rbdc::Error>
            where
                T: serde::Serialize
            {
                #[rbatis::py_sql(
                    "`select * from ${table_name} where id = #{id} limit 1`"
                )]
                async fn select_by_id_impl(
                    executor: &dyn rbatis::executor::Executor,
                    table_name: String,
                    id: &rbs::Value
                ) -> std::result::Result<Option<$table>, rbatis::rbdc::Error> {
                    rbatis::impled!()
                }

                let mut table_name = $table_name.to_string();
                if table_name.is_empty() {
                    #[rbatis::snake_name($table)]
                    fn snake_name() {}
                    table_name = snake_name();
                }
                let id_value = rbs::value!(id);  // 使用 value! 而不是 to_value!
                select_by_id_impl(executor, table_name, &id_value).await
            }
        }
    };
}

#[macro_export]
macro_rules! impl_update_by_id {
    ($table:ty) => {
        $crate::impl_update_by_id!($table, "");
    };
    ($table:ty, $table_name:expr) => {
        #[allow(non_local_definitions)]
        impl $table {
            pub async fn update_by_id(
                executor: &dyn rbatis::executor::Executor,
                table: &$table,
            ) -> std::result::Result<rbatis::rbdc::db::ExecResult, rbatis::rbdc::Error>
            {
                use rbatis::crud_traits::ValueOperatorSql;
                #[rbatis::py_sql(
                    "`update ${table_name}`  
                      set collection='table',skips='id':  
                      ` where id = #{table.id}`"
                )]
                async fn update_by_id_impl(
                    executor: &dyn rbatis::executor::Executor,
                    table_name: String,
                    table: &rbs::Value,
                ) -> std::result::Result<rbatis::rbdc::db::ExecResult, rbatis::rbdc::Error> {
                    rbatis::impled!()
                }

                let mut table_name = $table_name.to_string();
                if table_name.is_empty() {
                    #[rbatis::snake_name($table)]
                    fn snake_name() {}
                    table_name = snake_name();
                }
                let table_value = rbs::value!(table);
                update_by_id_impl(executor, table_name, &table_value).await
            }
        }
    };
}

#[macro_export]
macro_rules! impl_delete_by_id {
    ($table:ty) => {
        $crate::impl_delete_by_id!($table, "");
    };
    ($table:ty, $table_name:expr) => {
        #[allow(non_local_definitions)]
        impl $table {
            pub async fn delete_by_id<T>(
                executor: &dyn rbatis::executor::Executor,
                id: T,
            ) -> std::result::Result<rbatis::rbdc::db::ExecResult, rbatis::rbdc::Error>
            where
                T: serde::Serialize,
            {
                #[rbatis::py_sql("`delete from ${table_name} where id = #{id}`")]
                async fn delete_by_id_impl(
                    executor: &dyn rbatis::executor::Executor,
                    table_name: String,
                    id: &rbs::Value,
                ) -> std::result::Result<rbatis::rbdc::db::ExecResult, rbatis::rbdc::Error>
                {
                    rbatis::impled!()
                }

                let mut table_name = $table_name.to_string();
                if table_name.is_empty() {
                    #[rbatis::snake_name($table)]
                    fn snake_name() {}
                    table_name = snake_name();
                }
                let id_value = rbs::value!(id);
                delete_by_id_impl(executor, table_name, &id_value).await
            }
        }
    };
}

/// 示例：， column 和 order_by 可选
//    let condition = value! {
//             "handle_type": HandleType::THUMB_UP,
//             "column": ["*"],
//             "order_by": "created_date desc"
//         };
#[macro_export]
macro_rules! impl_select_page_by_condition {
    ($table:ty) => {
         $crate::impl_select_page_by_condition!($table, "");
    };
    ($table:ty, $table_name:expr) => {
        impl $table {
            pub async fn select_page_by_condition(
                executor: &dyn rbatis::executor::Executor,
                page_request: &dyn rbatis::plugin::IPageRequest,
                mut condition: rbs::Value
            ) -> std::result::Result<rbatis::plugin::Page<$table>, rbatis::rbdc::Error> {
                use rbatis::crud_traits::ValueOperatorSql;

                // 提取列规范和排序，并从条件中移除
                let (table_column, order_by) = {
                    let mut columns = String::new();
                    let mut order = String::new();
                    let mut clean_map = rbs::value::map::ValueMap::with_capacity(condition.len());

                    for (k, v) in condition {
                        match k.as_str() {
                            Some("column") => {
                                columns = match v {
                                    rbs::Value::String(s) => s.clone(),
                                    rbs::Value::Array(arr) => {
                                        let cols: Vec<&str> = arr.iter()
                                            .filter_map(|v| v.as_str())
                                            .collect();
                                        if cols.is_empty() { "*".to_string() } else { cols.join(", ") }
                                    }
                                    _ => "*".to_string(),
                                };
                            }
                            Some("order_by") => {
                                order = match v {
                                    rbs::Value::String(s) => s.clone(),
                                    _ => String::new(),
                                };
                            }
                            _ => {
                                clean_map.insert(k.clone(), v.clone());
                            }
                        }
                    }

                    if columns.is_empty() { columns = "*".to_string(); }
                    condition = rbs::Value::Map(clean_map);
                    (columns, order)
                };

                #[rbatis::py_sql(
                    "`select ${table_column} from ${table_name}`    
                     trim end=' where ':    
                       ` where `    
                       trim ' and ': for key,item in condition:    
                                    if item == null:    
                                       continue:    
                                    if !item.is_array():    
                                      ` and ${key.operator_sql()}#{item}`    
                                    if item.is_array():    
                                      ` and ${key} in (`    
                                         trim ',': for _,item_array in item:    
                                              #{item_array},    
                                      `)`    
                     if !order_by.is_empty():  
                       ` order by ${order_by}`"
                )]
                async fn select_page_by_condition_impl(
                    executor: &dyn rbatis::executor::Executor,
                    table_name: String,
                    table_column: &str,
                    condition: &rbs::Value,
                    order_by: &str
                ) -> std::result::Result<rbs::Value, rbatis::rbdc::Error> {  // 修改返回类型为 rbs::Value
                    for (_,v) in condition {
                        if v.is_array() && v.is_empty(){
                           return Ok(rbs::Value::Array(vec![]));  // 返回空数组而不是空 Vec
                        }
                    }
                    rbatis::impled!()
                }

                let mut table_name = $table_name.to_string();
                if table_name.is_empty() {
                    #[rbatis::snake_name($table)]
                    fn snake_name() {}
                    table_name = snake_name();
                }

                // 处理分页逻辑
                let mut executor = executor;
                let mut conn = None;
                if executor.name().eq(rbatis::executor::Executor::name(executor.rb_ref())){
                    conn = Some(executor.rb_ref().acquire().await?);
                    match &conn {
                        Some(c) => {
                            executor = c;
                        }
                        None => {}
                    }
                }

                let mut intercept = executor.rb_ref().get_intercept::<rbatis::plugin::intercept_page::PageIntercept>()
                    .ok_or_else(|| rbatis::rbdc::Error::from("PageIntercept not found"))?;

                let mut total = 0;
                if page_request.do_count() {
                    intercept.count_ids.insert(executor.id(), rbatis::plugin::PageRequest::new(page_request.page_no(), page_request.page_size()));
                    let total_value = select_page_by_condition_impl(executor, table_name.clone(), &table_column, &condition, &order_by).await?;
                    total = rbatis::decode(total_value).unwrap_or(0);
                }

                intercept.select_ids.insert(executor.id(), rbatis::plugin::PageRequest::new(page_request.page_no(), page_request.page_size()));
                let mut page = rbatis::plugin::Page::<$table>::new(page_request.page_no(), page_request.page_size(), total, vec![]);
                let records_value = select_page_by_condition_impl(executor, table_name, &table_column, &condition, &order_by).await?;
                page.records = rbs::from_value(records_value)?;  // 使用 rbs::from_value 而不是直接赋值
                Ok(page)
            }
        }
    };
}

#[macro_export]
macro_rules! plus {
    ($table:ty{}) => {
        $crate::impl_select_by_id!($table);
        $crate::impl_update_by_id!($table);
        $crate::impl_delete_by_id!($table);
        $crate::impl_select_one_by_condition!($table);
        $crate::impl_count_by_condition!($table);
        $crate::impl_select_page_by_condition!($table);
    };
    ($table:ty{}, $table_name:expr) => {
        $crate::impl_select_by_id!($table, $table_name);
        $crate::impl_update_by_id!($table, $table_name);
        $crate::impl_delete_by_id!($table, $table_name);
        $crate::impl_select_one_by_condition!($table, $table_name);
        $crate::impl_count_by_condition!($table, $table_name);
        $crate::impl_select_page_by_condition!($table, $table_name);
    };
}
