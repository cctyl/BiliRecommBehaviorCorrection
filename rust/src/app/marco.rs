#[macro_export]  
macro_rules! impl_select_one_by_condition {  
    ($table:ty) => {  
        impl_select_one_by_condition!($table, "");  
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

// #[macro_export]  
// macro_rules! impl_update_by_id {  
//     ($table:ty) => {  
//         $crate:: impl_update_by_id!($table, "");  
//     };  
//     ($table:ty, $table_name:expr) => {  
//         #[allow(non_local_definitions)]  
//         impl $table {  
//             pub async fn update_by_id<T>(  
//                 executor: &dyn rbatis::executor::Executor,  
//                 table: &$table,  
//                 id: T  
//             ) -> std::result::Result<rbatis::rbdc::db::ExecResult, rbatis::rbdc::Error>  
//             where  
//                 T: serde::Serialize  
//             {  
//                 use rbatis::crud_traits::ValueOperatorSql;  
//                 #[rbatis::py_sql(  
//                     "`update ${table_name}`  
//                       set collection='table',skips='id':  
//                       ` where id = #{id}`"  
//                 )]  
//                 async fn update_by_id_impl(  
//                     executor: &dyn rbatis::executor::Executor,  
//                     table_name: String,  
//                     table: &rbs::Value,  
//                     id: &rbs::Value  
//                 ) -> std::result::Result<rbatis::rbdc::db::ExecResult, rbatis::rbdc::Error> {  
//                     rbatis::impled!()  
//                 }  
                  
//                 let mut table_name = $table_name.to_string();  
//                 if table_name.is_empty() {  
//                     #[rbatis::snake_name($table)]  
//                     fn snake_name() {}  
//                     table_name = snake_name();  
//                 }  
//                 let table_value = rbs::value!(table);  
//                 let id_value = rbs::value!(id);  
//                 update_by_id_impl(executor, table_name, &table_value, &id_value).await  
//             }  
//         }  
//     };  
// }


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
                id: T  
            ) -> std::result::Result<rbatis::rbdc::db::ExecResult, rbatis::rbdc::Error>  
            where  
                T: serde::Serialize  
            {  
                #[rbatis::py_sql(  
                    "`delete from ${table_name} where id = #{id}`"  
                )]  
                async fn delete_by_id_impl(  
                    executor: &dyn rbatis::executor::Executor,  
                    table_name: String,  
                    id: &rbs::Value  
                ) -> std::result::Result<rbatis::rbdc::db::ExecResult, rbatis::rbdc::Error> {  
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

#[macro_export]      
macro_rules! plus {      
    ($table:ty{}) => {      
        $crate::impl_select_by_id!($table);      
        $crate::impl_update_by_id!($table);      
        $crate::impl_delete_by_id!($table);      
        $crate::impl_select_one_by_condition!($table);      
    };      
    ($table:ty{}, $table_name:expr) => {      
        $crate::impl_select_by_id!($table, $table_name);      
        $crate::impl_update_by_id!($table, $table_name);      
        $crate::impl_delete_by_id!($table, $table_name);      
        $crate::impl_select_one_by_condition!($table, $table_name);      
    };      
}