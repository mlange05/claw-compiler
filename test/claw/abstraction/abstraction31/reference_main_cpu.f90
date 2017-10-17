PROGRAM test_abstraction4
 USE mo_column , ONLY: compute , t1
 REAL :: q ( 1 : 20 , 1 : 60 )
 REAL :: z ( 1 : 20 )
 TYPE ( t1 ) :: ty
 INTEGER :: nproma
 INTEGER :: nz
 INTEGER :: p

 nproma = 20
 nz = 60
 z = 10.0
 DO p = 1 , nproma , 1
  q ( p , 1 ) = 0.0
 END DO
 ALLOCATE ( ty % y ( nproma , nz ) )
!$ACC data copyin(q,t) copyout(q,t)
 CALL compute ( nz , q ( : , 1 : 60 ) , ty % y ( : , : ) , z ( : ) , nproma =&
  nproma )
!$ACC end data
 PRINT * , sum ( q )
 PRINT * , sum ( ty % y )
 DEALLOCATE ( ty % y )
END PROGRAM test_abstraction4

