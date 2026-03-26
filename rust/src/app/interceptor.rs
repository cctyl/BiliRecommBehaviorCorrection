use log::info;
use rbatis::executor::Executor;  
use rbatis::intercept::{Intercept, ResultType};  
use rbatis::{async_trait, Action, Error};  
use rbs::Value;  
use std::sync::Arc;  
use rbatis::rbdc::db::ExecResult;
  
#[derive(Debug, Default)]  
pub struct SqlOnlyLogInterceptor {}  
  
#[async_trait]  
impl Intercept for SqlOnlyLogInterceptor {  
    async fn before(  
        &self,  
        task_id: i64,  
        _rb: &dyn Executor,  
        sql: &mut String,  
        args: &mut Vec<Value>,  
        _result: ResultType<&mut Result<ExecResult, Error>, &mut Result<Value, Error>>,  
    ) -> Result<Action, Error> {  
        info!("[rb] [{}] => `{}` {:?}", task_id, sql, args);  
        Ok(Action::Next)  
    }  
  
    async fn after(  
        &self,  
        _task_id: i64,  
        _rb: &dyn Executor,  
        _sql: &mut String,  
        _args: &mut Vec<Value>,  
        _result: ResultType<&mut Result<ExecResult, Error>, &mut Result<Value, Error>>,  
    ) -> Result<Action, Error> {  
        // 不输出任何结果信息  
        Ok(Action::Next)  
    }  
}